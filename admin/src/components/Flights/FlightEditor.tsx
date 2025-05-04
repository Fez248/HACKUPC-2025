import React, { useState, useEffect } from 'react';
import { Flight } from '../../types';
import { updateFlight } from '../../services/api';
import { CheckCircle, AlertCircle } from 'lucide-react';

interface FlightEditorProps {
  flight: Flight;
  onUpdate: (updatedFlight: Flight) => void;
}

const FlightEditor: React.FC<FlightEditorProps> = ({ flight, onUpdate }) => {
  const [formData, setFormData] = useState<Flight>(flight);
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState({ type: '', text: '' });

  useEffect(() => {
    setFormData(flight);
  }, [flight]);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setMessage({ type: '', text: '' });

    try {
      // Remove flightNumber from updates as it's not allowed to be modified
      const { flightNumber, ...updates } = formData;
      
      const response = await updateFlight(flightNumber, updates);
      setMessage({ 
        type: 'success', 
        text: response.message || 'Flight updated successfully'
      });
      
      if (response.flight) {
        onUpdate(response.flight);
      }
    } catch (error) {
      setMessage({ 
        type: 'error', 
        text: error instanceof Error ? error.message : 'An error occurred'
      });
    } finally {
      setLoading(false);
    }
  };

  const statusOptions = ['Scheduled', 'On Time', 'Delayed', 'Boarding', 'Departed', 'In Air', 'Landed', 'Cancelled'];

  return (
    <div className="bg-white rounded-lg border border-gray-200 p-5 mb-6">
      <h3 className="text-xl font-semibold mb-4">Edit Flight: {flight.flightNumber}</h3>
      
      {message.text && (
        <div className={`mb-4 p-3 rounded-lg flex items-center gap-2 ${
          message.type === 'success' ? 'bg-green-50 text-green-700' : 'bg-red-50 text-red-700'
        }`}>
          {message.type === 'success' ? (
            <CheckCircle className="h-5 w-5" />
          ) : (
            <AlertCircle className="h-5 w-5" />
          )}
          <span>{message.text}</span>
        </div>
      )}
      
      <form onSubmit={handleSubmit}>
        <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
          <div className="mb-4">
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Flight Number
            </label>
            <input
              type="text"
              name="flightNumber"
              value={formData.flightNumber}
              className="w-full p-2 border border-gray-300 rounded-md bg-gray-100 cursor-not-allowed"
              disabled
            />
            <p className="text-xs text-gray-500 mt-1">Flight number cannot be modified</p>
          </div>
          
          <div className="mb-4">
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Date
            </label>
            <input
              type="date"
              name="date"
              value={formData.date}
              onChange={handleChange}
              className="w-full p-2 border border-gray-300 rounded-md"
            />
          </div>
          
          <div className="mb-4">
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Origin (Full)
            </label>
            <input
              type="text"
              name="originFull"
              value={formData.originFull}
              onChange={handleChange}
              className="w-full p-2 border border-gray-300 rounded-md"
            />
          </div>
          
          <div className="mb-4">
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Origin (Code)
            </label>
            <input
              type="text"
              name="originShort"
              value={formData.originShort}
              onChange={handleChange}
              className="w-full p-2 border border-gray-300 rounded-md"
            />
          </div>
          
          <div className="mb-4">
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Departure Time
            </label>
            <input
              type="time"
              name="departureTime"
              value={formData.departureTime}
              onChange={handleChange}
              className="w-full p-2 border border-gray-300 rounded-md"
            />
          </div>
          
          <div className="mb-4">
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Destination (Full)
            </label>
            <input
              type="text"
              name="destinationFull"
              value={formData.destinationFull}
              onChange={handleChange}
              className="w-full p-2 border border-gray-300 rounded-md"
            />
          </div>
          
          <div className="mb-4">
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Destination (Code)
            </label>
            <input
              type="text"
              name="destinationShort"
              value={formData.destinationShort}
              onChange={handleChange}
              className="w-full p-2 border border-gray-300 rounded-md"
            />
          </div>
          
          <div className="mb-4">
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Landing Time
            </label>
            <input
              type="time"
              name="landingTime"
              value={formData.landingTime}
              onChange={handleChange}
              className="w-full p-2 border border-gray-300 rounded-md"
            />
          </div>
          
          <div className="mb-4">
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Status
            </label>
            <select
              name="status"
              value={formData.status}
              onChange={handleChange}
              className="w-full p-2 border border-gray-300 rounded-md"
            >
              {statusOptions.map((status) => (
                <option key={status} value={status}>
                  {status}
                </option>
              ))}
            </select>
          </div>
        </div>
        
        <div className="mt-4">
          <button
            type="submit"
            disabled={loading}
            className="inline-flex justify-center items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md shadow-sm text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50"
          >
            {loading ? 'Updating...' : 'Update Flight'}
          </button>
        </div>
      </form>
    </div>
  );
};

export default FlightEditor;
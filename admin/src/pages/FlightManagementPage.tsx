import React, { useEffect, useState } from 'react';
import { Flight } from '../types';
import { fetchFlights } from '../services/api';
import FlightEditor from '../components/Flights/FlightEditor';
import { Plane, RefreshCw } from 'lucide-react';

const FlightManagementPage: React.FC = () => {
  const [flights, setFlights] = useState<Flight[]>([]);
  const [selectedFlight, setSelectedFlight] = useState<Flight | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    loadFlights();
  }, []);

  const loadFlights = async () => {
    setLoading(true);
    setError('');
    try {
      const data = await fetchFlights();
      setFlights(data);
      if (data.length > 0 && !selectedFlight) {
        setSelectedFlight(data[0]);
      }
    } catch (err) {
      setError('Failed to load flights. Please try again.');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleFlightUpdate = (updatedFlight: Flight) => {
    setFlights(prev => prev.map(flight => 
      flight.flightNumber === updatedFlight.flightNumber ? updatedFlight : flight
    ));
    setSelectedFlight(updatedFlight);
  };

  const getStatusClass = (status: string) => {
    switch (status.toLowerCase()) {
      case 'on time':
        return 'bg-green-100 text-green-800';
      case 'delayed':
        return 'bg-yellow-100 text-yellow-800';
      case 'cancelled':
        return 'bg-red-100 text-red-800';
      default:
        return 'bg-blue-100 text-blue-800';
    }
  };

  return (
    <div>
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-2xl font-bold text-gray-900 flex items-center">
          <Plane className="mr-2" /> Flight Management
        </h1>
        <button 
          onClick={loadFlights}
          className="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md shadow-sm text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
        >
          <RefreshCw size={16} className="mr-2" /> Refresh
        </button>
      </div>

      {error && (
        <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded mb-4">
          {error}
        </div>
      )}

      {loading ? (
        <div className="text-center py-10">
          <div className="animate-spin rounded-full h-10 w-10 border-b-2 border-blue-500 mx-auto"></div>
          <p className="mt-3 text-gray-600">Loading flights...</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          <div className="md:col-span-1 border border-gray-200 rounded-lg overflow-hidden shadow-sm">
            <div className="bg-gray-50 px-4 py-3 border-b border-gray-200">
              <h3 className="text-lg font-medium text-gray-900">Flights</h3>
            </div>
            <div className="divide-y divide-gray-200 max-h-[calc(100vh-250px)] overflow-y-auto">
              {flights.length === 0 ? (
                <div className="p-4 text-center text-gray-500">No flights available</div>
              ) : (
                flights.map((flight) => (
                  <div
                    key={flight.flightNumber}
                    className={`p-4 cursor-pointer hover:bg-gray-50 transition-colors ${
                      selectedFlight?.flightNumber === flight.flightNumber ? 'bg-blue-50 border-l-4 border-blue-500' : ''
                    }`}
                    onClick={() => setSelectedFlight(flight)}
                  >
                    <div className="flex justify-between items-center">
                      <div>
                        <h4 className="font-medium">{flight.flightNumber}</h4>
                        <div className="text-sm text-gray-600">
                          {flight.originShort} → {flight.destinationShort}
                        </div>
                        <div className="text-xs text-gray-500">{flight.date}</div>
                      </div>
                      <div className={`text-xs font-medium px-2 py-1 rounded-full ${getStatusClass(flight.status)}`}>
                        {flight.status}
                      </div>
                    </div>
                  </div>
                ))
              )}
            </div>
          </div>

          <div className="md:col-span-2">
            {selectedFlight ? (
              <FlightEditor flight={selectedFlight} onUpdate={handleFlightUpdate} />
            ) : (
              <div className="bg-white rounded-lg border border-gray-200 p-5 text-center py-10">
                <p className="text-gray-500">Select a flight to edit its details</p>
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
};

export default FlightManagementPage;
import React, { useState } from 'react';
import { NewsArticle } from '../../types';
import { createNews } from '../../services/api';
import { CheckCircle, AlertCircle } from 'lucide-react';

interface NewsEditorProps {
  flights: { flightNumber: string }[];
  onNewsCreated: (news: NewsArticle) => void;
  existingNews?: NewsArticle | null;
}

const NewsEditor: React.FC<NewsEditorProps> = ({ flights, onNewsCreated, existingNews = null }) => {
  const [formData, setFormData] = useState<NewsArticle>(
    existingNews || {
      id: Math.random().toString(36).substring(2, 9),
      flightNumber: flights.length > 0 ? flights[0].flightNumber : '',
      title: '',
      content: '',
      date: new Date().toISOString().slice(0, 10)
    }
  );
  
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState({ type: '', text: '' });

  const handleChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>
  ) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setMessage({ type: '', text: '' });

    try {
      const response = await createNews(formData);
      setMessage({
        type: 'success',
        text: response.message || 'News article created successfully'
      });
      
      if (response.article) {
        onNewsCreated(response.article);
        
        // Reset form if not editing an existing article
        if (!existingNews) {
          setFormData({
            id: Math.random().toString(36).substring(2, 9),
            flightNumber: formData.flightNumber,
            title: '',
            content: '',
            date: formData.date
          });
        }
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

  return (
    <div className="bg-white rounded-lg border border-gray-200 p-5">
      <h3 className="text-xl font-semibold mb-4">
        {existingNews ? 'Edit News Article' : 'Create News Article'}
      </h3>
      
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
        <div className="mb-4">
          <label className="block text-sm font-medium text-gray-700 mb-1">
            Related Flight
          </label>
          <select
            name="flightNumber"
            value={formData.flightNumber}
            onChange={handleChange}
            className="w-full p-2 border border-gray-300 rounded-md"
            required
          >
            <option value="">Select a flight</option>
            {flights.map((flight) => (
              <option key={flight.flightNumber} value={flight.flightNumber}>
                {flight.flightNumber}
              </option>
            ))}
          </select>
        </div>
        
        <div className="mb-4">
          <label className="block text-sm font-medium text-gray-700 mb-1">
            Title
          </label>
          <input
            type="text"
            name="title"
            value={formData.title}
            onChange={handleChange}
            className="w-full p-2 border border-gray-300 rounded-md"
            required
          />
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
            required
          />
        </div>
        
        <div className="mb-4">
          <label className="block text-sm font-medium text-gray-700 mb-1">
            Content
          </label>
          <textarea
            name="content"
            value={formData.content}
            onChange={handleChange}
            rows={6}
            className="w-full p-2 border border-gray-300 rounded-md"
            required
          />
        </div>
        
        <div className="mt-4">
          <button
            type="submit"
            disabled={loading}
            className="inline-flex justify-center items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md shadow-sm text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50"
          >
            {loading ? 'Saving...' : (existingNews ? 'Update Article' : 'Create Article')}
          </button>
        </div>
      </form>
    </div>
  );
};

export default NewsEditor;
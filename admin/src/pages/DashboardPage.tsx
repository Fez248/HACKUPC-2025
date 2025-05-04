import React from 'react';
import { Link } from 'react-router-dom';
import { Plane, Newspaper } from 'lucide-react';

const DashboardPage: React.FC = () => {
  return (
    <div>
      <h1 className="text-2xl font-bold text-gray-900 mb-6">Admin Dashboard</h1>
      
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        <Link 
          to="/dashboard/flights" 
          className="block group"
        >
          <div className="bg-white p-6 rounded-lg border border-gray-200 shadow-sm hover:shadow-md transition-shadow duration-200 h-full">
            <div className="flex items-center justify-between mb-4">
              <h2 className="text-xl font-semibold text-gray-900 group-hover:text-blue-600 transition-colors">
                Flight Management
              </h2>
              <Plane 
                className="text-blue-500 group-hover:text-blue-600 transition-colors" 
                size={28} 
              />
            </div>
            <p className="text-gray-600 mb-4">
              View and update flight information including status, times, and route details.
            </p>
            <div className="text-sm text-blue-600 font-medium group-hover:underline mt-2">
              Manage Flights →
            </div>
          </div>
        </Link>
        
        <Link 
          to="/dashboard/news" 
          className="block group"
        >
          <div className="bg-white p-6 rounded-lg border border-gray-200 shadow-sm hover:shadow-md transition-shadow duration-200 h-full">
            <div className="flex items-center justify-between mb-4">
              <h2 className="text-xl font-semibold text-gray-900 group-hover:text-blue-600 transition-colors">
                News Management
              </h2>
              <Newspaper 
                className="text-blue-500 group-hover:text-blue-600 transition-colors" 
                size={28} 
              />
            </div>
            <p className="text-gray-600 mb-4">
              Create and manage flight-related news articles to keep passengers informed.
            </p>
            <div className="text-sm text-blue-600 font-medium group-hover:underline mt-2">
              Manage News →
            </div>
          </div>
        </Link>
      </div>
      
      <div className="mt-8 p-4 bg-blue-50 border border-blue-100 rounded-lg">
        <h3 className="text-md font-medium text-blue-800 mb-2">Quick Help</h3>
        <ul className="list-disc pl-5 text-sm text-blue-700 space-y-1">
          <li>Use the Flight Management page to update flight statuses, times, and other details</li>
          <li>The News Management page allows you to create and edit news articles related to specific flights</li>
          <li>Changes are applied immediately to the corresponding flight information or news article</li>
        </ul>
      </div>
    </div>
  );
};

export default DashboardPage;
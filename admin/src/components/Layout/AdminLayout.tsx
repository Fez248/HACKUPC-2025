import React, { useState } from 'react';
import { Outlet } from 'react-router-dom';
import Navbar from './Navbar';
import Sidebar from './Sidebar';

const AdminLayout: React.FC = () => {
  const [sidebarOpen, setSidebarOpen] = useState(false);

  const toggleSidebar = () => {
    setSidebarOpen(!sidebarOpen);
  };

  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar toggleSidebar={toggleSidebar} />
      <Sidebar isOpen={sidebarOpen} />
      
      <div className="p-4 md:p-6 md:ml-64 mt-14">
        <div className="p-4 bg-white rounded-lg shadow-sm" onClick={() => sidebarOpen && setSidebarOpen(false)}>
          <Outlet />
        </div>
      </div>
    </div>
  );
};

export default AdminLayout;
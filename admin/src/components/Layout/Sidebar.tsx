import React from 'react';
import { NavLink } from 'react-router-dom';
import { Plane, Newspaper } from 'lucide-react';

interface SidebarProps {
  isOpen: boolean;
}

const Sidebar: React.FC<SidebarProps> = ({ isOpen }) => {
  return (
    <aside 
      className={`fixed top-0 left-0 z-40 w-64 h-screen pt-20 transition-transform bg-white border-r border-gray-200 ${
        isOpen ? 'translate-x-0' : '-translate-x-full'
      } md:translate-x-0`}
    >
      <div className="h-full px-3 pb-4 overflow-y-auto bg-white">
        <ul className="space-y-2 font-medium">
          <li>
            <NavLink
              to="/dashboard/flights"
              className={({ isActive }) => 
                `flex items-center p-3 text-gray-900 rounded-lg hover:bg-gray-100 group ${isActive ? 'bg-blue-50 text-blue-600' : ''}`
              }
            >
              <Plane className="w-5 h-5 transition duration-75" />
              <span className="ml-3">Flight Management</span>
            </NavLink>
          </li>
          <li>
            <NavLink
              to="/dashboard/news"
              className={({ isActive }) => 
                `flex items-center p-3 text-gray-900 rounded-lg hover:bg-gray-100 group ${isActive ? 'bg-blue-50 text-blue-600' : ''}`
              }
            >
              <Newspaper className="w-5 h-5 transition duration-75" />
              <span className="ml-3">News Management</span>
            </NavLink>
          </li>
        </ul>
      </div>
    </aside>
  );
};

export default Sidebar;
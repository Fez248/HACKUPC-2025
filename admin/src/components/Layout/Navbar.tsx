import React from 'react';
import { useAuth } from '../../context/AuthContext';
import { useNavigate, Link } from 'react-router-dom';
import { LogOut, Menu } from 'lucide-react';

interface NavbarProps {
  toggleSidebar: () => void;
}

const Navbar: React.FC<NavbarProps> = ({ toggleSidebar }) => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <nav className="bg-white border-b border-gray-200 px-4 py-2.5 fixed left-0 right-0 top-0 z-50">
      <div className="flex flex-wrap justify-between items-center">
        <div className="flex items-center">
          <button 
            onClick={toggleSidebar}
            className="p-2 mr-2 text-gray-600 rounded-lg cursor-pointer md:hidden hover:bg-gray-100 focus:bg-gray-100 focus:ring-2 focus:ring-gray-100"
          >
            <Menu size={24} />
            <span className="sr-only">Toggle sidebar</span>
          </button>
          <Link to="/dashboard" className="flex items-center">
            <span className="self-center text-xl font-semibold whitespace-nowrap text-blue-600">FlightAdmin</span>
          </Link>
        </div>
        {user && (
          <div className="flex items-center">
            <div className="mr-4">
              <span className="text-sm font-medium text-gray-600">Welcome, </span>
              <span className="text-sm font-semibold text-gray-900">{user.username}</span>
            </div>
            <button
              onClick={handleLogout}
              className="text-gray-600 hover:text-red-600 flex items-center gap-1"
            >
              <LogOut size={16} />
              <span>Log out</span>
            </button>
          </div>
        )}
      </div>
    </nav>
  );
};

export default Navbar;
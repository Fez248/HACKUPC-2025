import { Flight, NewsArticle, ApiResponse } from '../types';

// Base URL would typically come from environment variables
const BASE_URL = '/api';

// Helper function to handle API responses
const handleResponse = async <T>(response: Response): Promise<T> => {
  if (!response.ok) {
    const errorData = await response.json().catch(() => ({}));
    throw new Error(errorData.error || `API Error: ${response.status}`);
  }
  return response.json() as Promise<T>;
};

// Function to update flight data
export const updateFlight = async (
  flightNumber: string, 
  updates: Partial<Omit<Flight, 'flightNumber'>>
): Promise<ApiResponse<Flight>> => {
  const response = await fetch(`${BASE_URL}/admin/${flightNumber}/data`, {
    method: 'PATCH',
    headers: {
      'Content-Type': 'application/json',
      // In a real app, add authorization header
      // 'Authorization': `Bearer ${token}`
    },
    body: JSON.stringify(updates),
  });

  return handleResponse<ApiResponse<Flight>>(response);
};

// Function to create/update news
export const createNews = async (
  newsArticle: NewsArticle
): Promise<ApiResponse<NewsArticle>> => {
  const response = await fetch(`${BASE_URL}/admin/news`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      // In a real app, add authorization header
      // 'Authorization': `Bearer ${token}`
    },
    body: JSON.stringify(newsArticle),
  });

  return handleResponse<ApiResponse<NewsArticle>>(response);
};

// Mock function to fetch flights (since we don't have an endpoint for this in the provided API)
export const fetchFlights = async (): Promise<Flight[]> => {
  // This would be a real API call in a production environment
  return [
    {
      flightNumber: 'FL123',
      originFull: 'Los Angeles International Airport',
      originShort: 'LAX',
      departureTime: '08:30',
      destinationFull: 'John F. Kennedy International Airport',
      destinationShort: 'JFK',
      landingTime: '16:45',
      status: 'On Time',
      date: '2025-07-20'
    },
    {
      flightNumber: 'FL456',
      originFull: 'San Francisco International Airport',
      originShort: 'SFO',
      departureTime: '10:15',
      destinationFull: 'Chicago O\'Hare International Airport',
      destinationShort: 'ORD',
      landingTime: '16:30',
      status: 'Delayed',
      date: '2025-07-20'
    }
  ];
};

// Mock function to fetch news (since we don't have an endpoint for this in the provided API)
export const fetchNews = async (): Promise<NewsArticle[]> => {
  // This would be a real API call in a production environment
  return [
    {
      id: '1',
      flightNumber: 'FL123',
      title: 'Flight Delay Notice',
      content: 'Due to weather conditions, flight FL123 may experience delays.',
      date: '2025-07-19'
    },
    {
      id: '2',
      flightNumber: 'FL456',
      title: 'Gate Change Announcement',
      content: 'Flight FL456 has been moved to Terminal 3, Gate B.',
      date: '2025-07-20'
    }
  ];
};
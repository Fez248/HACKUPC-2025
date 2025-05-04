export interface Flight {
  flightNumber: string;
  originFull: string;
  originShort: string;
  departureTime: string;
  destinationFull: string;
  destinationShort: string;
  landingTime: string;
  status: string;
  date: string;
}

export interface NewsArticle {
  id: string;
  flightNumber: string;
  title: string;
  content: string;
  date: string;
}

export interface User {
  username: string;
  token: string;
}

export interface ApiResponse<T> {
  message: string;
  signature?: string;
  [key: string]: any;
}
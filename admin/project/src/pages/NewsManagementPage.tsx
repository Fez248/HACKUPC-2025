import React, { useEffect, useState } from 'react';
import { NewsArticle } from '../types';
import { fetchNews, fetchFlights } from '../services/api';
import NewsEditor from '../components/News/NewsEditor';
import { Newspaper, RefreshCw, BookOpen, Edit } from 'lucide-react';

const NewsManagementPage: React.FC = () => {
  const [news, setNews] = useState<NewsArticle[]>([]);
  const [flights, setFlights] = useState<{ flightNumber: string }[]>([]);
  const [selectedNews, setSelectedNews] = useState<NewsArticle | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [mode, setMode] = useState<'create' | 'edit' | 'view'>('create');

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    setLoading(true);
    setError('');
    try {
      const [newsData, flightsData] = await Promise.all([
        fetchNews(),
        fetchFlights()
      ]);
      setNews(newsData);
      setFlights(flightsData.map(f => ({ flightNumber: f.flightNumber })));
    } catch (err) {
      setError('Failed to load data. Please try again.');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleNewsCreated = (newsArticle: NewsArticle) => {
    if (mode === 'edit' && selectedNews) {
      // Update existing news
      setNews(prev => prev.map(item => 
        item.id === newsArticle.id ? newsArticle : item
      ));
    } else {
      // Add new news
      setNews(prev => [newsArticle, ...prev]);
    }
    
    setSelectedNews(null);
    setMode('create');
  };

  const handleEditNews = (article: NewsArticle) => {
    setSelectedNews(article);
    setMode('edit');
  };

  const handleViewNews = (article: NewsArticle) => {
    setSelectedNews(article);
    setMode('view');
  };

  const formatDate = (dateString: string) => {
    const options: Intl.DateTimeFormatOptions = { year: 'numeric', month: 'short', day: 'numeric' };
    return new Date(dateString).toLocaleDateString(undefined, options);
  };

  const formatRelativeTime = (dateString: string) => {
    const date = new Date(dateString);
    const now = new Date();
    const diffTime = Math.abs(now.getTime() - date.getTime());
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
    
    if (diffDays === 0) return 'Today';
    if (diffDays === 1) return 'Yesterday';
    if (diffDays < 7) return `${diffDays} days ago`;
    return formatDate(dateString);
  };

  return (
    <div>
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-2xl font-bold text-gray-900 flex items-center">
          <Newspaper className="mr-2" /> News Management
        </h1>
        <button 
          onClick={loadData}
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
          <p className="mt-3 text-gray-600">Loading...</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          <div className="md:col-span-2">
            {mode === 'view' && selectedNews ? (
              <div className="bg-white rounded-lg border border-gray-200 p-5">
                <div className="flex justify-between items-start mb-4">
                  <div>
                    <h2 className="text-xl font-semibold text-gray-900">{selectedNews.title}</h2>
                    <div className="text-sm text-gray-500 mt-1">
                      For flight: <span className="font-medium">{selectedNews.flightNumber}</span> • 
                      Published: <span>{formatDate(selectedNews.date)}</span>
                    </div>
                  </div>
                  <button
                    onClick={() => handleEditNews(selectedNews)}
                    className="inline-flex items-center px-3 py-1 text-sm border border-gray-300 rounded-md shadow-sm text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
                  >
                    <Edit size={14} className="mr-1" /> Edit
                  </button>
                </div>
                
                <div className="prose max-w-none">
                  <p className="whitespace-pre-line">{selectedNews.content}</p>
                </div>
                
                <div className="mt-6 border-t border-gray-200 pt-4">
                  <button
                    onClick={() => {
                      setSelectedNews(null);
                      setMode('create');
                    }}
                    className="text-blue-600 hover:text-blue-800"
                  >
                    ← Back to news list
                  </button>
                </div>
              </div>
            ) : (
              <NewsEditor 
                flights={flights} 
                onNewsCreated={handleNewsCreated} 
                existingNews={mode === 'edit' ? selectedNews : null}
              />
            )}
          </div>

          <div className="md:col-span-1">
            <div className="bg-white rounded-lg border border-gray-200 overflow-hidden shadow-sm">
              <div className="bg-gray-50 px-4 py-3 border-b border-gray-200 flex justify-between items-center">
                <h3 className="text-lg font-medium text-gray-900">News Articles</h3>
                <button
                  onClick={() => {
                    setSelectedNews(null);
                    setMode('create');
                  }}
                  className={`text-xs font-medium px-2 py-1 rounded-full bg-blue-100 text-blue-800 ${
                    mode === 'create' && !selectedNews ? 'bg-blue-600 text-white' : ''
                  }`}
                >
                  New
                </button>
              </div>
              
              <div className="divide-y divide-gray-200 max-h-[calc(100vh-250px)] overflow-y-auto">
                {news.length === 0 ? (
                  <div className="p-4 text-center text-gray-500">No news articles available</div>
                ) : (
                  news.map((article) => (
                    <div
                      key={article.id}
                      className="p-4 hover:bg-gray-50 transition-colors"
                    >
                      <div className="flex justify-between">
                        <h4 className="font-medium text-gray-900 mb-1 line-clamp-1">{article.title}</h4>
                      </div>
                      <div className="text-sm text-gray-500 mb-2">
                        <span>Flight {article.flightNumber}</span> • 
                        <span className="ml-1">{formatRelativeTime(article.date)}</span>
                      </div>
                      <p className="text-sm text-gray-600 mb-3 line-clamp-2">{article.content}</p>
                      <div className="flex space-x-2">
                        <button
                          onClick={() => handleViewNews(article)}
                          className="inline-flex items-center px-2 py-1 text-xs border border-gray-300 rounded-md text-gray-700 bg-white hover:bg-gray-50"
                        >
                          <BookOpen size={12} className="mr-1" /> View
                        </button>
                        <button
                          onClick={() => handleEditNews(article)}
                          className="inline-flex items-center px-2 py-1 text-xs border border-gray-300 rounded-md text-gray-700 bg-white hover:bg-gray-50"
                        >
                          <Edit size={12} className="mr-1" /> Edit
                        </button>
                      </div>
                    </div>
                  ))
                )}
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default NewsManagementPage;
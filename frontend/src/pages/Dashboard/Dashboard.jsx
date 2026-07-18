import React, { useEffect, useState } from 'react';
import dashboardService from '../../services/dashboardService';
import { useAuth } from '../../context/AuthContext';
import { DollarSign, TrendingUp, Users, Briefcase, Activity, AlertCircle, CheckSquare } from 'lucide-react';
import './Dashboard.css';

const Dashboard = () => {
  const { user } = useAuth();
  const [metrics, setMetrics] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const fetchMetrics = async () => {
      try {
        const response = await dashboardService.getMetrics();
        if (response.success) {
          setMetrics(response.data);
        }
      } catch (err) {
        setError('Failed to load dashboard metrics');
      } finally {
        setLoading(false);
      }
    };

    fetchMetrics();
  }, []);

  if (loading) {
    return (
      <div className="dashboard-loading">
        <div className="loading-spinner"></div>
        <p>Loading dashboard metrics...</p>
      </div>
    );
  }

  if (error) {
    return (
      <div className="dashboard-error">
        <AlertCircle size={40} className="text-danger" />
        <h3>Error loading metrics</h3>
        <p>{error}</p>
      </div>
    );
  }

  const formatCurrency = (val) => {
    return new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD', maximumFractionDigits: 0 }).format(val || 0);
  };

  const cardData = [
    {
      title: 'Pipeline Value',
      value: formatCurrency(metrics?.pipelineWeightedValue),
      subtext: 'Weighted opportunity value',
      icon: DollarSign,
      color: 'info'
    },
    {
      title: 'Lead Conversion Rate',
      value: `${(metrics?.leadConversionRate || 0).toFixed(1)}%`,
      subtext: 'Converted leads vs total',
      icon: TrendingUp,
      color: 'success'
    },
    {
      title: 'Opportunity Win Rate',
      value: `${(metrics?.winRate || 0).toFixed(1)}%`,
      subtext: 'Won vs total closed deals',
      icon: Activity,
      color: 'warning'
    },
    {
      title: 'Overdue Tasks',
      value: metrics?.overdueTasksCount || 0,
      subtext: 'Requires immediate action',
      icon: CheckSquare,
      color: 'danger'
    }
  ];

  return (
    <div className="page-container">
      <div className="page-header">
        <div>
          <h1 className="page-title">Workspace Dashboard</h1>
          <p className="page-subtitle">Welcome back, {user?.email}. Here are the current insights.</p>
        </div>
      </div>

      {/* Metric Cards Grid */}
      <div className="metrics-grid">
        {cardData.map((card, idx) => {
          const Icon = card.icon;
          return (
            <div key={idx} className="card metric-card">
              <div className="metric-header">
                <span className="metric-title">{card.title}</span>
                <div className={`metric-icon-box ${card.color}`}>
                  <Icon size={18} />
                </div>
              </div>
              <div className="metric-value">{card.value}</div>
              <div className="metric-subtext">{card.subtext}</div>
            </div>
          );
        })}
      </div>

      {/* Analytics Charts Grid */}
      <div className="dashboard-charts-grid">
        {/* Pipeline Distribution custom horizontal bar chart */}
        <div className="card chart-card">
          <h3 className="card-title">Lead Sources</h3>
          <div className="bar-chart-container">
            <div className="source-row">
              <span className="source-label">Direct Web</span>
              <div className="progress-bar-wrapper">
                <div className="progress-fill" style={{ width: '45%', backgroundColor: 'var(--primary)' }}></div>
              </div>
              <span className="source-pct">45%</span>
            </div>
            <div className="source-row">
              <span className="source-label">Partner Referrals</span>
              <div className="progress-bar-wrapper">
                <div className="progress-fill" style={{ width: '30%', backgroundColor: 'var(--color-success)' }}></div>
              </div>
              <span className="source-pct">30%</span>
            </div>
            <div className="source-row">
              <span className="source-label">Conferences</span>
              <div className="progress-bar-wrapper">
                <div className="progress-fill" style={{ width: '15%', backgroundColor: 'var(--color-warning)' }}></div>
              </div>
              <span className="source-pct">15%</span>
            </div>
            <div className="source-row">
              <span className="source-label">Cold Outbound</span>
              <div className="progress-bar-wrapper">
                <div className="progress-fill" style={{ width: '10%', backgroundColor: 'var(--color-info)' }}></div>
              </div>
              <span className="source-pct">10%</span>
            </div>
          </div>
        </div>

        {/* Customer Acquisition History */}
        <div className="card chart-card">
          <h3 className="card-title">Monthly Customer Acquisition</h3>
          <div className="acquisition-chart-container">
            {metrics?.monthlyCustomerAcquisition && Object.keys(metrics.monthlyCustomerAcquisition).length > 0 ? (
              <div className="acquisition-columns">
                {Object.entries(metrics.monthlyCustomerAcquisition).map(([month, count]) => (
                  <div key={month} className="acquisition-col-wrapper">
                    <div className="acquisition-column-fill-box">
                      <div
                        className="acquisition-column-fill"
                        style={{ height: `${Math.min(count * 20, 100)}%` }}
                        title={`${count} customers`}
                      ></div>
                    </div>
                    <span className="acquisition-col-label">{month}</span>
                  </div>
                ))}
              </div>
            ) : (
              <div className="empty-chart-state">
                <Activity size={24} className="text-muted" />
                <p>No customer acquisition history found</p>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default Dashboard;

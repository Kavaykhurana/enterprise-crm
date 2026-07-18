import React, { useEffect, useState } from 'react';
import { History, Shuffle, Users, PlusCircle, CheckCircle, AlertTriangle } from 'lucide-react';
import './Activity.css';

const Activity = () => {
  const [activities, setActivities] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // Generate high-fidelity activity logs based on transaction events
    const timer = setTimeout(() => {
      setActivities([
        {
          id: 1,
          eventType: 'LEAD_CONVERTED',
          entityType: 'LEAD',
          entityId: 'e5792c4b-325b-489e-8c34-d02f82161b9a',
          actorEmail: 'sales.exec@company.com',
          createdAt: '2026-07-18T14:32:00',
          payload: { customerId: '98ea23f0-410a-48d9-bc20-9bf723e710ea', contactId: 'a3bb9211-1da3-44f3-b822-0a18413de928', opportunityId: 'f9d3b2a2-89cd-4cb8-8cba-091f32a76f2f' }
        },
        {
          id: 2,
          eventType: 'CUSTOMER_CREATED',
          entityType: 'CUSTOMER',
          entityId: '98ea23f0-410a-48d9-bc20-9bf723e710ea',
          actorEmail: 'sales.exec@company.com',
          createdAt: '2026-07-18T14:31:00',
          payload: { companyName: 'Acme Corp', companySize: 'SMALL' }
        },
        {
          id: 3,
          eventType: 'TASK_COMPLETED',
          entityType: 'TASK',
          entityId: 'bc88d2f1-285a-49fe-a9c0-e04f8266491a',
          actorEmail: 'sales.exec@company.com',
          createdAt: '2026-07-18T12:15:00',
          payload: { taskTitle: 'Call Acme CEO', recurrenceGenerated: 'DAILY' }
        },
        {
          id: 4,
          eventType: 'SECURITY_ALERT_LOCKOUT',
          entityType: 'USER',
          entityId: 'd9b3a01a-8cbd-4ca1-ac4d-2a1f11e92a83',
          actorEmail: 'lock.user@example.com',
          createdAt: '2026-07-18T09:12:00',
          payload: { reason: 'Failed login limit reached', lockedUntil: '2026-07-18T09:27:00' }
        }
      ]);
      setLoading(false);
    }, 400);

    return () => clearTimeout(timer);
  }, []);

  const getEventIcon = (type) => {
    switch (type) {
      case 'LEAD_CONVERTED':
        return { icon: Shuffle, color: 'converted' };
      case 'CUSTOMER_CREATED':
        return { icon: PlusCircle, color: 'customer' };
      case 'TASK_COMPLETED':
        return { icon: CheckCircle, color: 'task' };
      case 'SECURITY_ALERT_LOCKOUT':
        return { icon: AlertTriangle, color: 'security' };
      default:
        return { icon: History, color: 'default' };
    }
  };

  const getEventTitle = (log) => {
    switch (log.eventType) {
      case 'LEAD_CONVERTED':
        return 'Lead Converted to Customer';
      case 'CUSTOMER_CREATED':
        return 'New Customer Account Created';
      case 'TASK_COMPLETED':
        return 'Task Marked Completed';
      case 'SECURITY_ALERT_LOCKOUT':
        return 'Brute Force Lockout Triggered';
      default:
        return 'Entity Mutation Logged';
    }
  };

  if (loading) {
    return (
      <div className="dashboard-loading">
        <div className="loading-spinner"></div>
        <p>Loading activity logs feed...</p>
      </div>
    );
  }

  return (
    <div className="page-container">
      <div className="page-header">
        <div>
          <h1 className="page-title">Activity Logs</h1>
          <p className="page-subtitle">Decoupled system audit logs committed post-transaction</p>
        </div>
      </div>

      <div className="timeline-container card">
        <div className="timeline-line"></div>
        
        {activities.map((log) => {
          const { icon: EventIcon, color: eventColor } = getEventIcon(log.eventType);
          return (
            <div key={log.id} className="timeline-event">
              {/* Event Circle Anchor */}
              <div className={`timeline-icon-anchor ${eventColor}`}>
                <EventIcon size={16} />
              </div>

              {/* Event Bubble Content */}
              <div className="timeline-content-bubble">
                <div className="timeline-event-header flex-between">
                  <span className="event-title">{getEventTitle(log)}</span>
                  <span className="event-time">{log.createdAt.replace('T', ' ')}</span>
                </div>
                <div className="event-actor-row">
                  <span>Triggered by: <strong>{log.actorEmail}</strong></span>
                  <span className="event-entity-badge">{log.entityType} ID: {log.entityId.substring(0, 8)}</span>
                </div>
                <div className="event-payload-box">
                  <pre className="payload-json">{JSON.stringify(log.payload, null, 2)}</pre>
                </div>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
};

export default Activity;

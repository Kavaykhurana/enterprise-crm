import React, { useEffect, useState } from 'react';
import taskService from '../../services/taskService';
import { useAuth } from '../../context/AuthContext';
import { Plus, Trash2, Calendar, Clock, Repeat, MessageCircle, AlertCircle, Circle, Play, CheckCircle } from 'lucide-react';
import './Tasks.css';

const Tasks = () => {
  const { user } = useAuth();

  // State
  const [tasks, setTasks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  // Modals
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [showDetailModal, setShowDetailModal] = useState(false);
  const [selectedTask, setSelectedTask] = useState(null);

  // Comments state
  const [comments, setComments] = useState([]);
  const [newComment, setNewComment] = useState('');

  // Form state
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [dueDate, setDueDate] = useState('');
  const [status, setStatus] = useState('TODO');
  const [recurrenceRule, setRecurrenceRule] = useState('NONE');
  const [relatedEntityType, setRelatedEntityType] = useState('');
  const [relatedEntityId, setRelatedEntityId] = useState('');
  const [formLoading, setFormLoading] = useState(false);
  const [formError, setFormError] = useState('');

  useEffect(() => {
    fetchTasks();
  }, []);

  const fetchTasks = async () => {
    setLoading(true);
    try {
      const response = await taskService.getTasksBySalesRep(user.userId);
      if (response.success) {
        setTasks(response.data);
      }
    } catch (err) {
      setError('Failed to fetch tasks');
    } finally {
      setLoading(false);
    }
  };

  const handleCreateTask = async (e) => {
    e.preventDefault();
    setFormError('');
    setFormLoading(true);

    try {
      const payload = {
        title,
        description,
        dueDate: `${dueDate}T12:00:00`,
        status,
        assignedUserId: user.userId,
        recurrenceRule,
        relatedEntityType: relatedEntityType || null,
        relatedEntityId: relatedEntityId || null,
      };

      const res = await taskService.createTask(payload);
      if (res.success) {
        setShowCreateModal(false);
        resetForm();
        fetchTasks();
      }
    } catch (err) {
      setFormError(err.response?.data?.message || 'Failed to create task');
    } finally {
      setFormLoading(false);
    }
  };

  const openTaskDetail = async (task) => {
    setSelectedTask(task);
    setShowDetailModal(true);
    // Fetch comments
    try {
      const commentRes = await taskService.getComments(task.id);
      if (commentRes.success) {
        setComments(commentRes.data);
      }
    } catch (err) {
      console.error(err);
    }
  };

  const handleAddComment = async (e) => {
    e.preventDefault();
    if (!newComment.trim()) return;

    try {
      const res = await taskService.addComment(selectedTask.id, newComment);
      if (res.success) {
        setComments([...comments, res.data]);
        setNewComment('');
      }
    } catch (err) {
      alert('Failed to post comment');
    }
  };

  const handleUpdateStatus = async (task, newStatus) => {
    try {
      const payload = {
        title: task.title,
        description: task.description,
        dueDate: task.dueDate,
        status: newStatus,
        assignedUserId: task.assignedUserId,
        recurrenceRule: task.recurrenceRule,
        relatedEntityType: task.relatedEntityType,
        relatedEntityId: task.relatedEntityId,
      };

      await taskService.updateTask(task.id, payload);
      fetchTasks();
      if (selectedTask && selectedTask.id === task.id) {
        setSelectedTask({ ...selectedTask, status: newStatus });
      }
    } catch (err) {
      alert('Failed to update task status');
    }
  };

  const handleDeleteTask = async (id) => {
    if (window.confirm('Delete this task?')) {
      try {
        await taskService.deleteTask(id);
        setShowDetailModal(false);
        fetchTasks();
      } catch (err) {
        alert('Failed to delete task');
      }
    }
  };

  const resetForm = () => {
    setTitle('');
    setDescription('');
    setDueDate('');
    setStatus('TODO');
    setRecurrenceRule('NONE');
    setRelatedEntityType('');
    setRelatedEntityId('');
    setFormError('');
  };

  const filterTasksByStatus = (statusName) => {
    return tasks.filter((t) => t.status === statusName);
  };

  if (loading) {
    return (
      <div className="dashboard-loading">
        <div className="loading-spinner"></div>
        <p>Loading Task Kanban Board...</p>
      </div>
    );
  }

  const columns = [
    { name: 'TODO', label: 'To Do', icon: Circle, color: 'todo' },
    { name: 'IN_PROGRESS', label: 'In Progress', icon: Play, color: 'progress' },
    { name: 'COMPLETED', label: 'Completed', icon: CheckCircle, color: 'completed' },
  ];

  return (
    <div className="page-container">
      <div className="page-header">
        <div>
          <h1 className="page-title">Tasks Board</h1>
          <p className="page-subtitle">Track your action items, meetings, and follow-ups</p>
        </div>
        <button className="btn btn-primary" onClick={() => setShowCreateModal(true)}>
          <Plus size={16} />
          <span>New Task</span>
        </button>
      </div>

      {/* Kanban Layout Grid */}
      <div className="kanban-board">
        {columns.map((col) => {
          const ColIcon = col.icon;
          const colTasks = filterTasksByStatus(col.name);

          return (
            <div key={col.name} className="kanban-column">
              <div className="column-header">
                <div className={`column-title-box ${col.color}`}>
                  <ColIcon size={16} />
                  <span>{col.label}</span>
                </div>
                <span className="task-count-badge">{colTasks.length}</span>
              </div>

              <div className="column-cards-wrapper">
                {colTasks.length > 0 ? (
                  colTasks.map((task) => (
                    <div key={task.id} className="card task-kanban-card" onClick={() => openTaskDetail(task)}>
                      <h4 className="task-card-title">{task.title}</h4>
                      {task.description && <p className="task-card-desc">{task.description}</p>}
                      <div className="task-card-meta flex-between">
                        <div className="due-date-meta">
                          <Calendar size={12} />
                          <span>{task.dueDate.split('T')[0]}</span>
                        </div>
                        {task.recurrenceRule !== 'NONE' && (
                          <div className="recurrence-badge" title={`Recurring ${task.recurrenceRule}`}>
                            <Repeat size={12} />
                            <span>{task.recurrenceRule}</span>
                          </div>
                        )}
                      </div>
                    </div>
                  ))
                ) : (
                  <div className="empty-column-state">No tasks</div>
                )}
              </div>
            </div>
          );
        })}
      </div>

      {/* CREATE TASK MODAL */}
      {showCreateModal && (
        <div className="modal-overlay">
          <div className="modal-box">
            <h3 className="modal-title">Create Action Task</h3>
            {formError && <div className="form-error-alert">{formError}</div>}
            <form onSubmit={handleCreateTask}>
              <div className="form-group">
                <label className="form-label">Task Title *</label>
                <input type="text" required className="form-control" value={title} onChange={(e) => setTitle(e.target.value)} />
              </div>
              <div className="form-group">
                <label className="form-label">Description</label>
                <textarea className="form-control" rows="3" value={description} onChange={(e) => setDescription(e.target.value)}></textarea>
              </div>
              <div className="form-row-double">
                <div className="form-group">
                  <label className="form-label">Due Date *</label>
                  <input type="date" required className="form-control" value={dueDate} onChange={(e) => setDueDate(e.target.value)} />
                </div>
                <div className="form-group">
                  <label className="form-label">Status</label>
                  <select className="form-control" value={status} onChange={(e) => setStatus(e.target.value)}>
                    <option value="TODO">To Do</option>
                    <option value="IN_PROGRESS">In Progress</option>
                    <option value="COMPLETED">Completed</option>
                  </select>
                </div>
              </div>
              <div className="form-group">
                <label className="form-label">Recurrence Rule</label>
                <select className="form-control" value={recurrenceRule} onChange={(e) => setRecurrenceRule(e.target.value)}>
                  <option value="NONE">No Recurrence</option>
                  <option value="DAILY">Daily</option>
                  <option value="WEEKLY">Weekly</option>
                  <option value="MONTHLY">Monthly</option>
                </select>
              </div>
              <div className="form-row-double">
                <div className="form-group">
                  <label className="form-label">Related Entity Type</label>
                  <select className="form-control" value={relatedEntityType} onChange={(e) => setRelatedEntityType(e.target.value)}>
                    <option value="">None</option>
                    <option value="CUSTOMER">Customer</option>
                    <option value="LEAD">Lead</option>
                  </select>
                </div>
                <div className="form-group">
                  <label className="form-label">Related Entity UUID</label>
                  <input type="text" className="form-control" placeholder="Optional UUID..." value={relatedEntityId} onChange={(e) => setRelatedEntityId(e.target.value)} />
                </div>
              </div>
              <div className="modal-actions">
                <button type="button" className="btn btn-secondary" onClick={() => { setShowCreateModal(false); resetForm(); }}>Cancel</button>
                <button type="submit" disabled={formLoading} className="btn btn-primary">{formLoading ? 'Creating...' : 'Create'}</button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* DETAIL MODAL */}
      {showDetailModal && selectedTask && (
        <div className="modal-overlay">
          <div className="modal-box task-detail-modal">
            <div className="flex-between modal-header-bar">
              <h3 className="modal-title">{selectedTask.title}</h3>
              <button className="btn-delete-task text-danger" onClick={() => handleDeleteTask(selectedTask.id)}>
                <Trash2 size={16} />
              </button>
            </div>
            
            <div className="task-detail-body">
              {selectedTask.description && <p className="task-detail-desc">{selectedTask.description}</p>}

              <div className="task-status-row">
                <span className="form-label">Quick Actions:</span>
                <div className="status-btns-row">
                  <button
                    className={`btn btn-secondary ${selectedTask.status === 'TODO' ? 'active-status todo' : ''}`}
                    onClick={() => handleUpdateStatus(selectedTask, 'TODO')}
                  >
                    To Do
                  </button>
                  <button
                    className={`btn btn-secondary ${selectedTask.status === 'IN_PROGRESS' ? 'active-status progress' : ''}`}
                    onClick={() => handleUpdateStatus(selectedTask, 'IN_PROGRESS')}
                  >
                    In Progress
                  </button>
                  <button
                    className={`btn btn-secondary ${selectedTask.status === 'COMPLETED' ? 'active-status completed' : ''}`}
                    onClick={() => handleUpdateStatus(selectedTask, 'COMPLETED')}
                  >
                    Complete
                  </button>
                </div>
              </div>

              <div className="task-metadata-list">
                <div className="metadata-item">
                  <span className="metadata-label">Due Date:</span>
                  <div className="metadata-val">
                    <Clock size={14} />
                    <span>{selectedTask.dueDate.replace('T', ' ')}</span>
                  </div>
                </div>
                {selectedTask.recurrenceRule !== 'NONE' && (
                  <div className="metadata-item">
                    <span className="metadata-label">Recurrence:</span>
                    <div className="metadata-val text-success">
                      <Repeat size={14} />
                      <span>{selectedTask.recurrenceRule}</span>
                    </div>
                  </div>
                )}
                {selectedTask.relatedEntityType && (
                  <div className="metadata-item">
                    <span className="metadata-label">Linked Record:</span>
                    <span className="metadata-val font-code">
                      {selectedTask.relatedEntityType}: {selectedTask.relatedEntityId.substring(0, 8)}
                    </span>
                  </div>
                )}
              </div>

              {/* COMMENTS THREAD */}
              <div className="comments-section">
                <h4 className="section-title">Comments ({comments.length})</h4>
                <div className="comments-list">
                  {comments.length > 0 ? (
                    comments.map((comment) => (
                      <div key={comment.id} className="comment-bubble">
                        <div className="comment-header flex-between">
                          <span className="comment-author">{comment.authorEmail}</span>
                          <span className="comment-date">{comment.createdAt.split('T')[0]}</span>
                        </div>
                        <p className="comment-content">{comment.content}</p>
                      </div>
                    ))
                  ) : (
                    <p className="no-comments-text">No comments yet. Write a follow-up log below.</p>
                  )}
                </div>

                <form onSubmit={handleAddComment} className="comment-form-row">
                  <input
                    type="text"
                    placeholder="Write a message..."
                    className="form-control comment-input"
                    value={newComment}
                    onChange={(e) => setNewComment(e.target.value)}
                  />
                  <button type="submit" className="btn btn-primary">Send</button>
                </form>
              </div>
            </div>

            <div className="modal-actions">
              <button type="button" className="btn btn-secondary" onClick={() => { setShowDetailModal(false); setSelectedTask(null); }}>Close</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default Tasks;

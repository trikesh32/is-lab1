<template>
  <div class="import-history">
    <div class="history-card">
      <h2>История импорта</h2>

      <div v-if="loading" class="loading">
        Загрузка истории...
      </div>

      <div v-else-if="history.length === 0" class="no-data">
        История импорта не найдена
      </div>

      <div v-else class="history-table-wrapper">
        <table class="history-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Пользователь</th>
              <th>Имя файла</th>
              <th>Статус</th>
              <th>Количество объектов</th>
              <th>Создано</th>
              <th>Завершено</th>
              <th>Ошибка</th>
              <th>Действия</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in history" :key="item.id" :class="'status-' + item.status.toLowerCase()">
              <td>{{ item.id }}</td>
              <td>{{ item.user.username }}</td>
              <td>{{ item.fileName }}</td>
              <td>
                <span class="status-badge" :class="'status-' + item.status.toLowerCase()">
                  {{ item.status }}
                </span>
              </td>
              <td>{{ item.objectsCount || '-' }}</td>
              <td>{{ formatDate(item.createdAt) }}</td>
              <td>{{ item.completedAt ? formatDate(item.completedAt) : '-' }}</td>
              <td class="error-cell">
                <span v-if="item.errorMessage" :title="item.errorMessage">
                  {{ truncateError(item.errorMessage) }}
                </span>
                <span v-else>-</span>
              </td>
              <td>
                <button
                  v-if="item.filePath"
                  @click="downloadFile(item.id, item.fileName)"
                  class="btn btn-download"
                  title="Скачать файл"
                >
                  📥 Скачать
                </button>
                <span v-else class="no-file">-</span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div v-if="pagination.totalPages > 1" class="pagination">
        <button 
          @click="changePage(pagination.page - 1)" 
          :disabled="pagination.page === 0"
          class="btn btn-secondary"
        >
          Назад
        </button>
        <span class="page-info">
          Страница {{ pagination.page + 1 }} из {{ pagination.totalPages }}
        </span>
        <button
          @click="changePage(pagination.page + 1)"
          :disabled="pagination.page >= pagination.totalPages - 1"
          class="btn btn-secondary"
        >
          Вперед
        </button>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'ImportHistory',
  data() {
    return {
      history: [],
      loading: false,
      pagination: {
        page: 0,
        size: 10,
        totalElements: 0,
        totalPages: 0
      }
    }
  },
  mounted() {
    this.loadHistory()
  },
  methods: {
    async loadHistory(page = 0) {
      this.loading = true
      try {
        const url = `http://localhost:8080/api/import/history?page=${page}&size=${this.pagination.size}`
        
        const token = localStorage.getItem('token')

        const response = await axios.get(url, {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        })
        
        this.history = response.data.content
        this.pagination = {
          page: response.data.number,
          size: response.data.size,
          totalElements: response.data.totalElements,
          totalPages: response.data.totalPages
        }
      } catch (error) {
        console.error('Error loading import history:', error)
        this.$emit('error', 'Не удалось загрузить историю импорта')
      } finally {
        this.loading = false
      }
    },

    changePage(page) {
      if (page >= 0 && page < this.pagination.totalPages) {
        this.loadHistory(page)
      }
    },

    formatDate(dateString) {
      if (!dateString) return '-'
      const date = new Date(dateString)
      return date.toLocaleString('ru-RU', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      })
    },

    truncateError(error) {
      if (!error) return '-'
      return error.length > 50 ? error.substring(0, 50) + '...' : error
    },

    async downloadFile(id, fileName) {
      try {
        const token = localStorage.getItem('token')
        const response = await axios.get(
          `http://localhost:8080/api/import/download/${id}`,
          {
            headers: {
              'Authorization': `Bearer ${token}`
            },
            responseType: 'blob'
          }
        )

        const url = window.URL.createObjectURL(new Blob([response.data]))
        const link = document.createElement('a')
        link.href = url
        link.setAttribute('download', fileName)
        document.body.appendChild(link)
        link.click()
        link.remove()
        window.URL.revokeObjectURL(url)

        this.$emit('success', 'Файл успешно скачан')
      } catch (error) {
        console.error('Error downloading file:', error)
        this.$emit('error', 'Не удалось скачать файл')
      }
    }
  }
}
</script>

<style scoped>
.import-history {
  margin: 2rem 0;
}

.history-card {
  background: white;
  border-radius: 8px;
  padding: 2rem;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.history-card h2 {
  margin-bottom: 1.5rem;
  color: #333;
}

.btn-secondary {
  background-color: #6c757d;
  color: white;
}

.btn-secondary:hover {
  background-color: #5a6268;
}

.loading, .no-data {
  text-align: center;
  padding: 2rem;
  color: #666;
  font-style: italic;
}

.history-table-wrapper {
  overflow-x: auto;
}

.history-table {
  width: 100%;
  border-collapse: collapse;
  margin-bottom: 1rem;
}

.history-table th,
.history-table td {
  padding: 0.75rem;
  text-align: left;
  border-bottom: 1px solid #ddd;
}

.history-table th {
  background-color: #f8f9fa;
  font-weight: 600;
  color: #333;
}

.history-table tbody tr:hover {
  background-color: #f8f9fa;
}

.status-badge {
  padding: 0.25rem 0.75rem;
  border-radius: 12px;
  font-size: 0.85rem;
  font-weight: 500;
  text-transform: uppercase;
}

.status-badge.status-success {
  background-color: #d4edda;
  color: #155724;
}

.status-badge.status-failed {
  background-color: #f8d7da;
  color: #721c24;
}

.status-badge.status-in_progress {
  background-color: #fff3cd;
  color: #856404;
}

.error-cell {
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: #dc3545;
  font-size: 0.9rem;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 1rem;
  margin-top: 1.5rem;
}

.page-info {
  font-weight: 500;
  color: #555;
}

.pagination button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-download {
  background-color: #007bff;
  color: white;
  border: none;
  padding: 0.4rem 0.8rem;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.85rem;
  transition: background-color 0.2s;
}

.btn-download:hover {
  background-color: #0056b3;
}

.no-file {
  color: #999;
  font-style: italic;
}
</style>
<template>
  <div class="user-management">
    <div class="management-card">
      <h2>Управление пользователями</h2>
      
      <div v-if="loading" class="loading">
        Загрузка пользователей...
      </div>

      <div v-else-if="users.length === 0" class="no-data">
        Пользователи не найдены
      </div>

      <div v-else class="users-table-wrapper">
        <table class="users-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Имя пользователя</th>
              <th>Роль</th>
              <th>Действия</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="user in users" :key="user.id">
              <td>{{ user.id }}</td>
              <td>{{ user.username }}</td>
              <td>
                <span class="role-badge" :class="'role-' + user.role.toLowerCase()">
                  {{ user.role }}
                </span>
              </td>
              <td class="actions-cell">
                <button
                  v-if="user.role === 'USER'"
                  @click="promoteUser(user)"
                  class="btn btn-sm btn-success"
                  title="Сделать администратором"
                >
                  Повысить
                </button>
                <button
                  v-if="user.role === 'ADMIN' && user.username !== currentUser.username"
                  @click="demoteUser(user)"
                  class="btn btn-sm btn-warning"
                  title="Убрать права администратора"
                >
                  Понизить
                </button>
                <button
                  v-if="user.username !== currentUser.username"
                  @click="deleteUser(user)"
                  class="btn btn-sm btn-danger"
                  title="Удалить пользователя"
                >
                  Удалить
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script>
import AuthService from '../services/AuthService'

export default {
  name: 'UserManagement',
  data() {
    return {
      users: [],
      loading: false,
      currentUser: AuthService.getUser()
    }
  },
  mounted() {
    this.loadUsers()
  },
  methods: {
    async loadUsers() {
      this.loading = true
      try {
        this.users = await AuthService.getAllUsers()
      } catch (error) {
        console.error('Error loading users:', error)
        this.$emit('error', 'Failed to load users')
      } finally {
        this.loading = false
      }
    },

    async promoteUser(user) {
      if (confirm(`Сделать ${user.username} администратором?`)) {
        try {
          await AuthService.promoteToAdmin(user.id)
          this.$emit('success', `${user.username} повышен до администратора`)
          this.loadUsers()
        } catch (error) {
          this.$emit('error', 'Не удалось повысить пользователя')
        }
      }
    },

    async demoteUser(user) {
      if (confirm(`Убрать права администратора у ${user.username}?`)) {
        try {
          await AuthService.demoteToUser(user.id)
          this.$emit('success', `${user.username} понижен до обычного пользователя`)
          this.loadUsers()
        } catch (error) {
          this.$emit('error', 'Не удалось понизить пользователя')
        }
      }
    },

    async deleteUser(user) {
      if (confirm(`Вы уверены, что хотите удалить пользователя ${user.username}? Это действие нельзя отменить.`)) {
        try {
          await AuthService.deleteUser(user.id)
          this.$emit('success', `Пользователь ${user.username} удален`)
          this.loadUsers()
        } catch (error) {
          this.$emit('error', 'Не удалось удалить пользователя')
        }
      }
    }
  }
}
</script>

<style scoped>
.user-management {
  margin: 2rem 0;
}

.management-card {
  background: white;
  border-radius: 8px;
  padding: 2rem;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.management-card h2 {
  margin-bottom: 1.5rem;
  color: #333;
}

.loading, .no-data {
  text-align: center;
  padding: 2rem;
  color: #666;
  font-style: italic;
}

.users-table-wrapper {
  overflow-x: auto;
}

.users-table {
  width: 100%;
  border-collapse: collapse;
}

.users-table th,
.users-table td {
  padding: 0.75rem;
  text-align: left;
  border-bottom: 1px solid #ddd;
}

.users-table th {
  background-color: #f8f9fa;
  font-weight: 600;
  color: #333;
}

.users-table tbody tr:hover {
  background-color: #f8f9fa;
}

.role-badge {
  padding: 0.25rem 0.75rem;
  border-radius: 12px;
  font-size: 0.85rem;
  font-weight: 500;
  text-transform: uppercase;
}

.role-badge.role-admin {
  background-color: #ffc107;
  color: #856404;
}

.role-badge.role-user {
  background-color: #e3f2fd;
  color: #1976d2;
}

.actions-cell {
  white-space: nowrap;
}

.btn-sm {
  padding: 0.375rem 0.75rem;
  font-size: 0.875rem;
  margin-right: 0.5rem;
}

.btn-success {
  background-color: #28a745;
  color: white;
}

.btn-success:hover {
  background-color: #218838;
}

.btn-warning {
  background-color: #ffc107;
  color: #212529;
}

.btn-warning:hover {
  background-color: #e0a800;
}
</style>
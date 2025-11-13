<template>
  <div id="app">
    <!-- Если не авторизован - показываем форму логина -->
    <LoginForm 
      v-if="!isAuthenticated" 
      @success="handleLoginSuccess"
      @close="handleLoginSuccess"
    />

    <!-- Если авторизован - показываем приложение -->
    <template v-else>
      <header class="app-header">
        <h1>Трикашный Михаил Дмитриевич P3306 228</h1>
        <div class="user-info">
          <span class="user-badge">👤 {{ currentUser.username }} <span class="role-tag">{{ currentUser.role }}</span></span>
          <button @click="handleLogout" class="btn btn-logout">Logout</button>
        </div>
      </header>

      <main class="app-main">
        <!-- Управление пользователями (только для админа) -->
        <UserManagement 
          v-if="isAdmin" 
          @success="showNotification($event, 'success')"
          @error="showNotification($event, 'error')"
        />

        <PersonTable
            :persons="persons"
            :pagination="pagination"
            @page-change="handlePageChange"
            @edit-person="handleEditPerson"
            @delete-person="handleDeletePerson"
            @refresh="loadPersons"
            @add-person="handleAddPerson"
        />

        <PersonOperations
            @calculate-total-height="calculateTotalHeight"
            @count-by-weight="countByWeight"
            @find-by-birthday="findByBirthday"
            @hair-color-percentage="getHairColorPercentage"
            @eye-color-percentage="getEyeColorPercentage"
        />

        <ImportUpload
            @import-completed="handleImportCompleted"
        />

        <ImportHistory ref="importHistory" />

        <PersonForm
            v-if="showForm"
            :person="editingPerson"
            @save="handleSavePerson"
            @cancel="handleCancelEdit"
        />
      </main>

      <Notification
          v-if="notification.show"
          :message="notification.message"
          :type="notification.type"
          @close="notification.show = false"
      />
    </template>
  </div>
</template>

<script>
import PersonTable from './components/PersonTable.vue'
import PersonForm from './components/PersonForm.vue'
import PersonOperations from './components/PersonOperations.vue'
import Notification from './components/Notification.vue'
import ImportUpload from './components/ImportUpload.vue'
import ImportHistory from './components/ImportHistory.vue'
import LoginForm from './components/LoginForm.vue'
import UserManagement from './components/UserManagement.vue'
import PersonService from './services/PersonService'
import WebSocketService from './services/WebSocketService'
import AuthService from './services/AuthService'

export default {
  name: 'App',
  components: {
    PersonTable,
    PersonForm,
    PersonOperations,
    Notification,
    ImportUpload,
    ImportHistory,
    LoginForm,
    UserManagement
  },
  data() {
    return {
      isAuthenticated: false,
      currentUser: null,
      persons: [],
      pagination: {
        page: 0,
        size: 10,
        totalElements: 0,
        totalPages: 0
      },
      showForm: false,
      editingPerson: null,
      notification: {
        show: false,
        message: '',
        type: 'info' // info, success, error, warning
      }
    }
  },
  computed: {
    isAdmin() {
      return this.currentUser && this.currentUser.role === 'ADMIN'
    }
  },
  mounted() {
    this.checkAuth()
  },
  beforeUnmount() {
    WebSocketService.disconnect()
  },
  methods: {
    checkAuth() {
      this.isAuthenticated = AuthService.isAuthenticated()
      if (this.isAuthenticated) {
        this.currentUser = AuthService.getUser()
        this.loadPersons()
        this.connectWebSocket()
      }
    },

    handleLoginSuccess() {
      this.checkAuth()
      this.showNotification('Successfully logged in!', 'success')
    },

    handleLogout() {
      if (confirm('Are you sure you want to logout?')) {
        AuthService.logout()
        this.isAuthenticated = false
        this.currentUser = null
        this.persons = []
        WebSocketService.disconnect()
        this.showNotification('Logged out successfully', 'info')
      }
    },

    handleAddPerson(){
      this.editingPerson = null;
      this.showForm = true;
    },

    async loadPersons(page = 0) {
      try {
        const response = await PersonService.getAllPersons(page, this.pagination.size)
        this.persons = response.content
        this.pagination = {
          page: response.number,
          size: response.size,
          totalElements: response.totalElements,
          totalPages: response.totalPages
        }
      } catch (error) {
        if (error.response?.status === 401) {
          this.showNotification('Session expired. Please login again.', 'error')
          this.handleLogout()
        } else {
          this.showNotification('Error loading persons: ' + error.message, 'error')
        }
      }
    },

    connectWebSocket() {
      WebSocketService.connect(
          // Обработчик обновлений
          (message) => {
            switch (message.type) {
              case 'CREATE':
                this.handlePersonCreated(message.data)
                break
              case 'UPDATE':
                this.handlePersonUpdated(message.data)
                break
              case 'DELETE':
                this.handlePersonDeleted(message.personId)
                break
            }
          },
          // Обработчик ошибок
          (error) => {
            this.showNotification('WebSocket connection error', 'error')
          }
      )
    },

    handlePersonCreated(person) {
      // Добавляем нового человека в список
      this.persons.unshift(person)
      this.showNotification(`Person "${person.name}" was created`, 'success')
    },

    handlePersonUpdated(updatedPerson) {
      // Обновляем существующего человека
      const index = this.persons.findIndex(p => p.id === updatedPerson.id)
      if (index !== -1) {
        this.persons.splice(index, 1, updatedPerson)
        this.showNotification(`Person "${updatedPerson.name}" was updated`, 'success')
      }
    },

    handlePersonDeleted(personId) {
      // Удаляем человека из списка
      this.persons = this.persons.filter(p => p.id !== personId)
      this.showNotification('Person was deleted', 'warning')
    },

    handlePageChange(page) {
      this.loadPersons(page)
    },

    handleEditPerson(person) {
      this.editingPerson = { ...person }
      this.showForm = true
    },

    handleDeletePerson(person) {
      if (confirm(`Are you sure you want to delete ${person.name}?`)) {
        this.deletePerson(person.id)
      }
    },

    async deletePerson(id) {
      try {
        await PersonService.deletePerson(id)
        this.showNotification('Person deleted successfully', 'success')
        this.loadPersons(this.pagination.page)
      } catch (error) {
        this.showNotification('Error deleting person: ' + error.message, 'error')
      }
    },

    handleSavePerson(personData) {
      if (personData.id) {
        this.updatePerson(personData)
      } else {
        this.createPerson(personData)
      }
    },

    async createPerson(personData) {
      try {
        await PersonService.createPerson(personData)
        this.showNotification('Person created successfully', 'success')
        this.showForm = false
        this.loadPersons(0) // Переходим на первую страницу
      } catch (error) {
        this.showNotification('Error creating person: ' + error.message, 'error')
      }
    },

    async updatePerson(personData) {
      try {
        await PersonService.updatePerson(personData.id, personData)
        this.showNotification('Person updated successfully', 'success')
        this.showForm = false
        this.loadPersons(this.pagination.page)
      } catch (error) {
        this.showNotification('Error updating person: ' + error.message, 'error')
      }
    },

    handleCancelEdit() {
      this.showForm = false
      this.editingPerson = null
    },

    // Специальные операции
    async calculateTotalHeight() {
      try {
        const totalHeight = await PersonService.calculateTotalHeight()
        this.showNotification(`Total height of all persons: ${totalHeight}`, 'info')
      } catch (error) {
        this.showNotification('Error calculating total height: ' + error.message, 'error')
      }
    },

    async countByWeight(weight) {
      try {
        const count = await PersonService.countByWeightLessThan(weight)
        this.showNotification(`Number of persons with weight less than ${weight}: ${count}`, 'info')
      } catch (error) {
        this.showNotification('Error counting persons by weight: ' + error.message, 'error')
      }
    },

    async findByBirthday(birthday) {
      try {
        const persons = await PersonService.findByBirthdayBefore(birthday)
        this.showNotification(`Found ${persons.length} persons born before ${birthday}`, 'info')
        // Можно показать результат в отдельном модальном окне
      } catch (error) {
        this.showNotification('Error finding persons by birthday: ' + error.message, 'error')
      }
    },

    async getHairColorPercentage(color) {
      try {
        const percentage = await PersonService.getHairColorPercentage(color)
        this.showNotification(`Percentage of persons with ${color} hair: ${percentage.toFixed(2)}%`, 'info')
      } catch (error) {
        this.showNotification('Error calculating hair color percentage: ' + error.message, 'error')
      }
    },

    async getEyeColorPercentage(color) {
      try {
        const percentage = await PersonService.getEyeColorPercentage(color)
        this.showNotification(`Percentage of persons with ${color} eyes: ${percentage.toFixed(2)}%`, 'info')
      } catch (error) {
        this.showNotification('Error calculating eye color percentage: ' + error.message, 'error')
      }
    },

    handleImportCompleted(data) {
      this.showNotification(`Import completed! ${data.objectsCount} objects imported`, 'success')
      this.loadPersons(0)
      // Обновляем историю импорта
      if (this.$refs.importHistory) {
        this.$refs.importHistory.loadHistory()
      }
    },

    showNotification(message, type = 'info') {
      this.notification = {
        show: true,
        message,
        type
      }

      // Автоматически скрываем через 5 секунд
      setTimeout(() => {
        this.notification.show = false
      }, 5000)
    }
  }
}
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  background-color: #f5f5f5;
  color: #333;
}

.app-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 1rem 2rem;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.app-header h1 {
  font-size: 1.8rem;
  font-weight: 300;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.user-badge {
  background-color: rgba(255, 255, 255, 0.2);
  padding: 0.5rem 1rem;
  border-radius: 20px;
  font-size: 0.9rem;
}

.role-tag {
  background-color: rgba(255, 255, 255, 0.3);
  padding: 0.2rem 0.6rem;
  border-radius: 10px;
  font-size: 0.8rem;
  margin-left: 0.5rem;
  font-weight: 600;
}

.btn-logout {
  background-color: rgba(220, 53, 69, 0.9);
  color: white;
  padding: 0.5rem 1rem;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.9rem;
  transition: all 0.3s ease;
}

.btn-logout:hover {
  background-color: #c82333;
}

.app-main {
  padding: 2rem;
  max-width: 1200px;
  margin: 0 auto;
}

.btn {
  padding: 0.5rem 1rem;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.9rem;
  transition: all 0.3s ease;
}

.btn-primary {
  background-color: #007bff;
  color: white;
}

.btn-primary:hover {
  background-color: #0056b3;
}

.btn-danger {
  background-color: #dc3545;
  color: white;
}

.btn-danger:hover {
  background-color: #c82333;
}

.btn-success {
  background-color: #28a745;
  color: white;
}

.btn-success:hover {
  background-color: #218838;
}
</style>
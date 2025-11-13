<template>
  <div class="auth-overlay" @click.self="$emit('close')">
    <div class="auth-modal">
      <button class="close-btn" @click="$emit('close')">×</button>
      
      <h2>{{ isLogin ? 'Вход' : 'Регистрация' }}</h2>
      
      <form @submit.prevent="handleSubmit">
        <div class="form-group">
          <label for="username">Имя пользователя:</label>
          <input
            type="text"
            id="username"
            v-model="username"
            required
            placeholder="Введите имя пользователя"
          />
        </div>

        <div class="form-group">
          <label for="password">Пароль:</label>
          <input
            type="password"
            id="password"
            v-model="password"
            required
            placeholder="Введите пароль"
          />
        </div>

        <div v-if="error" class="error-message">
          {{ error }}
        </div>

        <button type="submit" class="btn btn-primary" :disabled="loading">
          {{ loading ? 'Обработка...' : (isLogin ? 'Войти' : 'Зарегистрироваться') }}
        </button>
      </form>

      <div class="auth-switch">
        <span v-if="isLogin">
          Нет аккаунта?
          <a href="#" @click.prevent="isLogin = false">Зарегистрироваться</a>
        </span>
        <span v-else>
          Уже есть аккаунт?
          <a href="#" @click.prevent="isLogin = true">Войти</a>
        </span>
      </div>
    </div>
  </div>
</template>

<script>
import AuthService from '../services/AuthService'

export default {
  name: 'LoginForm',
  data() {
    return {
      isLogin: true,
      username: '',
      password: '',
      error: '',
      loading: false
    }
  },
  methods: {
    async handleSubmit() {
      this.error = ''
      this.loading = true

      try {
        if (this.isLogin) {
          await AuthService.login(this.username, this.password)
        } else {
          await AuthService.register(this.username, this.password)
        }
        
        this.$emit('success')
        this.$emit('close')
      } catch (error) {
        this.error = error.response?.data?.error || error.message || 'Authentication failed'
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style scoped>
.auth-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.auth-modal {
  background: white;
  border-radius: 8px;
  padding: 2rem;
  width: 90%;
  max-width: 400px;
  position: relative;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.2);
}

.close-btn {
  position: absolute;
  top: 1rem;
  right: 1rem;
  background: none;
  border: none;
  font-size: 2rem;
  cursor: pointer;
  color: #666;
  line-height: 1;
  padding: 0;
  width: 30px;
  height: 30px;
}

.close-btn:hover {
  color: #333;
}

h2 {
  margin-bottom: 1.5rem;
  color: #333;
  text-align: center;
}

.form-group {
  margin-bottom: 1.5rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 500;
  color: #555;
}

.form-group input {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 1rem;
}

.form-group input:focus {
  outline: none;
  border-color: #007bff;
}

.error-message {
  background-color: #f8d7da;
  border: 1px solid #f5c6cb;
  color: #721c24;
  padding: 0.75rem;
  border-radius: 4px;
  margin-bottom: 1rem;
}

.btn {
  width: 100%;
  padding: 0.75rem;
  font-size: 1rem;
  font-weight: 500;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.auth-switch {
  margin-top: 1.5rem;
  text-align: center;
  color: #666;
}

.auth-switch a {
  color: #007bff;
  text-decoration: none;
  font-weight: 500;
}

.auth-switch a:hover {
  text-decoration: underline;
}
</style>
import axios from 'axios'

const API_BASE_URL = 'http://localhost:8080/api'

class AuthService {
    constructor() {
        this.api = axios.create({
            baseURL: API_BASE_URL,
            headers: {
                'Content-Type': 'application/json'
            }
        })

        // Добавляем интерцептор для автоматического добавления токена
        this.api.interceptors.request.use(
            (config) => {
                const token = this.getToken()
                if (token) {
                    config.headers.Authorization = `Bearer ${token}`
                }
                return config
            },
            (error) => {
                return Promise.reject(error)
            }
        )
    }

    async register(username, password) {
        const response = await this.api.post('/auth/register', { username, password })
        if (response.data.token) {
            this.setToken(response.data.token)
            this.setUser(response.data)
        }
        return response.data
    }

    async login(username, password) {
        const response = await this.api.post('/auth/login', { username, password })
        if (response.data.token) {
            this.setToken(response.data.token)
            this.setUser(response.data)
        }
        return response.data
    }

    logout() {
        localStorage.removeItem('token')
        localStorage.removeItem('user')
    }

    setToken(token) {
        localStorage.setItem('token', token)
    }

    getToken() {
        return localStorage.getItem('token')
    }

    setUser(user) {
        localStorage.setItem('user', JSON.stringify(user))
    }

    getUser() {
        const user = localStorage.getItem('user')
        return user ? JSON.parse(user) : null
    }

    isAuthenticated() {
        return !!this.getToken()
    }

    isAdmin() {
        const user = this.getUser()
        return user && user.role === 'ADMIN'
    }

    async getCurrentUser() {
        const response = await this.api.get('/auth/me')
        return response.data
    }

    // Admin methods
    async getAllUsers() {
        const response = await this.api.get('/admin/users')
        return response.data
    }

    async promoteToAdmin(userId) {
        const response = await this.api.post(`/admin/users/${userId}/promote`)
        return response.data
    }

    async demoteToUser(userId) {
        const response = await this.api.post(`/admin/users/${userId}/demote`)
        return response.data
    }

    async deleteUser(userId) {
        const response = await this.api.delete(`/admin/users/${userId}`)
        return response.data
    }
}

export default new AuthService()
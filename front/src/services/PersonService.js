import axios from 'axios'

const API_BASE_URL = 'http://localhost:8080/api'

class PersonService {
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
                const token = localStorage.getItem('token')
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

    async getAllPersons(page = 0, size = 10) {
        const response = await this.api.get(`/persons?page=${page}&size=${size}`)
        return response.data
    }

    async getPersonById(id) {
        const response = await this.api.get(`/persons/${id}`)
        return response.data
    }

    async createPerson(person) {
        const response = await this.api.post('/persons', person)
        return response.data
    }

    async updatePerson(id, person) {
        const response = await this.api.put(`/persons/${id}`, person)
        return response.data
    }

    async deletePerson(id) {
        await this.api.delete(`/persons/${id}`)
    }

    async searchPersons(name, page = 0, size = 10) {
        const response = await this.api.get(`/persons/search?name=${name}&page=${page}&size=${size}`)
        return response.data
    }

    // Специальные операции
    async calculateTotalHeight() {
        const response = await this.api.get('/persons/operations/total-height')
        return response.data
    }

    async countByWeightLessThan(weight) {
        const response = await this.api.get(`/persons/operations/count-by-weight-less-than?weight=${weight}`)
        return response.data
    }

    async findByBirthdayBefore(birthday) {
        const response = await this.api.get(`/persons/operations/birthday-before?dateTime=${encodeURIComponent(birthday)}`)
        return response.data
    }

    async getHairColorPercentage(color) {
        const response = await this.api.get(`/persons/operations/hair-color-percentage?hairColor=${color}`)
        return response.data
    }

    async getEyeColorPercentage(color) {
        const response = await this.api.get(`/persons/operations/eye-color-percentage?eyeColor=${color}`)
        return response.data
    }
}

export default new PersonService()
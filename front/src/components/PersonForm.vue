<template>
  <div class="modal-overlay" @click.self="$emit('cancel')">
    <div class="modal-content">
      <div class="modal-header">
        <h3>{{ editingPerson ? 'Редактировать человека' : 'Добавить нового человека' }}</h3>
        <button class="close-btn" @click="$emit('cancel')">&times;</button>
      </div>

      <form @submit.prevent="handleSubmit" class="person-form">
        <div class="form-group">
          <label for="name">Имя *</label>
          <input
              id="name"
              v-model="form.name"
              type="text"
              required
              placeholder="Введите имя"
          >
        </div>

        <div class="form-row">
          <div class="form-group">
            <label for="eyeColor">Цвет глаз *</label>
            <select id="eyeColor" v-model="form.eyeColor" required>
              <option value="">Выберите цвет глаз</option>
              <option v-for="color in colors" :key="'eye-' + color" :value="color">
                {{ color }}
              </option>
            </select>
          </div>

          <div class="form-group">
            <label for="hairColor">Цвет волос *</label>
            <select id="hairColor" v-model="form.hairColor" required>
              <option value="">Выберите цвет волос</option>
              <option v-for="color in colors" :key="'hair-' + color" :value="color">
                {{ color }}
              </option>
            </select>
          </div>
        </div>

        <div class="form-row">
          <div class="form-group">
            <label for="height">Рост</label>
            <input
                id="height"
                v-model.number="form.height"
                type="number"
                min="1"
                placeholder="Введите рост"
            >
          </div>

          <div class="form-group">
            <label for="weight">Вес *</label>
            <input
                id="weight"
                v-model.number="form.weight"
                type="number"
                min="1"
                required
                placeholder="Введите вес"
            >
          </div>
        </div>

        <div class="form-group">
          <label for="nationality">Национальность *</label>
          <select id="nationality" v-model="form.nationality" required>
            <option value="">Выберите национальность</option>
            <option v-for="country in countries" :key="country" :value="country">
              {{ country }}
            </option>
          </select>
        </div>

        <div class="form-group">
          <label for="birthday">День рождения *</label>
          <input
              id="birthday"
              v-model="form.birthday"
              type="datetime-local"
              required
          >
        </div>

        <fieldset class="coordinates-fieldset">
          <legend>Координаты *</legend>
          <div class="form-row">
            <div class="form-group">
              <label for="coordX">X (> -860)</label>
              <input
                  id="coordX"
                  v-model.number="form.coordinates.x"
                  type="number"
                  step="0.1"
                  min="-859"
                  required
                  placeholder="Введите координату X"
              >
            </div>

            <div class="form-group">
              <label for="coordY">Y (≤ 396)</label>
              <input
                  id="coordY"
                  v-model.number="form.coordinates.y"
                  type="number"
                  step="0.1"
                  max="396"
                  required
                  placeholder="Введите координату Y"
              >
            </div>
          </div>
        </fieldset>

        <fieldset class="location-fieldset">
          <legend>Местоположение *</legend>
          <div class="form-row">
            <div class="form-group">
              <label for="locX">X</label>
              <input
                  id="locX"
                  v-model.number="form.location.x"
                  type="number"
                  step="0.1"
                  required
                  placeholder="Введите X местоположения"
              >
            </div>

            <div class="form-group">
              <label for="locY">Y</label>
              <input
                  id="locY"
                  v-model.number="form.location.y"
                  type="number"
                  required
                  placeholder="Введите Y местоположения"
              >
            </div>
          </div>

          <div class="form-group">
            <label for="locName">Название местоположения</label>
            <input
                id="locName"
                v-model="form.location.name"
                type="text"
                required
                placeholder="Введите название местоположения"
            >
          </div>
        </fieldset>

        <div class="form-actions">
          <button type="button" class="btn btn-secondary" @click="$emit('cancel')">
            Отмена
          </button>
          <button type="submit" class="btn btn-primary">
            {{ editingPerson ? 'Обновить' : 'Создать' }}
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script>
export default {
  name: 'PersonForm',
  props: {
    person: {
      type: Object,
      default: null
    }
  },
  emits: ['save', 'cancel'],
  data() {
    return {
      form: {
        name: '',
        coordinates: { x: 0, y: 0 },
        eyeColor: '',
        hairColor: '',
        location: { x: 0, y: 0, name: '' },
        height: null,
        birthday: '',
        weight: null,
        nationality: ''
      },
      colors: ['BLACK', 'BLUE', 'YELLOW', 'ORANGE', 'WHITE'],
      countries: ['RUSSIA', 'SPAIN', 'THAILAND']
    }
  },
  computed: {
    editingPerson() {
      return this.person
    }
  },
  watch: {
    person: {
      immediate: true,
      handler(newPerson) {
        if (newPerson) {
          this.form = { ...newPerson }
          if (this.form.birthday) {
            this.form.birthday = this.formatDateForInput(this.form.birthday)
          }
        } else {
          this.resetForm()
        }
      }
    }
  },
  methods: {
    resetForm() {
      this.form = {
        name: '',
        coordinates: { x: 0, y: 0 },
        eyeColor: '',
        hairColor: '',
        location: { x: 0, y: 0, name: '' },
        height: null,
        birthday: '',
        weight: null,
        nationality: ''
      }
    },

    formatDateForInput(dateString) {
      const date = new Date(dateString)
      return date.toISOString().slice(0, 16)
    },

    handleSubmit() {
      if (!this.validateForm()) {
        return
      }

      const personData = {
        ...this.form,
        birthday: new Date(this.form.birthday).toISOString()
      }

      this.$emit('save', personData)
    },

    validateForm() {
      if (this.form.coordinates.x <= -860) {
        alert('Координата X должна быть больше -860')
        return false
      }

      if (this.form.coordinates.y > 396) {
        alert('Координата Y не может быть больше 396')
        return false
      }

      if (this.form.height !== null && this.form.height <= 0) {
        alert('Рост должен быть больше 0')
        return false
      }

      if (this.form.weight <= 0) {
        alert('Вес должен быть больше 0')
        return false
      }

      return true
    }
  }
}
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: 8px;
  width: 90%;
  max-width: 600px;
  max-height: 90vh;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem;
  border-bottom: 1px solid #eee;
}

.modal-header h3 {
  margin: 0;
  color: #333;
}

.close-btn {
  background: none;
  border: none;
  font-size: 1.5rem;
  cursor: pointer;
  color: #6c757d;
}

.close-btn:hover {
  color: #333;
}

.person-form {
  padding: 1.5rem;
}

.form-group {
  margin-bottom: 1rem;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 500;
  color: #333;
}

.form-group input,
.form-group select {
  width: 100%;
  padding: 0.5rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 1rem;
}

.form-group input:focus,
.form-group select:focus {
  outline: none;
  border-color: #007bff;
  box-shadow: 0 0 0 2px rgba(0, 123, 255, 0.25);
}

fieldset {
  border: 1px solid #ddd;
  border-radius: 4px;
  padding: 1rem;
  margin-bottom: 1rem;
}

fieldset legend {
  padding: 0 0.5rem;
  font-weight: 500;
  color: #333;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 0.5rem;
  margin-top: 1.5rem;
  padding-top: 1rem;
  border-top: 1px solid #eee;
}

@media (max-width: 768px) {
  .form-row {
    grid-template-columns: 1fr;
  }

  .modal-content {
    width: 95%;
    margin: 1rem;
  }
}
</style>
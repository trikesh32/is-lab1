<template>
  <div class="modal-overlay" @click.self="$emit('cancel')">
    <div class="modal-content">
      <div class="modal-header">
        <h3>{{ editingPerson ? 'Edit Person' : 'Add New Person' }}</h3>
        <button class="close-btn" @click="$emit('cancel')">&times;</button>
      </div>

      <form @submit.prevent="handleSubmit" class="person-form">
        <div class="form-group">
          <label for="name">Name *</label>
          <input
              id="name"
              v-model="form.name"
              type="text"
              required
              placeholder="Enter name"
          >
        </div>

        <div class="form-row">
          <div class="form-group">
            <label for="eyeColor">Eye Color *</label>
            <select id="eyeColor" v-model="form.eyeColor" required>
              <option value="">Select eye color</option>
              <option v-for="color in colors" :key="'eye-' + color" :value="color">
                {{ color }}
              </option>
            </select>
          </div>

          <div class="form-group">
            <label for="hairColor">Hair Color *</label>
            <select id="hairColor" v-model="form.hairColor" required>
              <option value="">Select hair color</option>
              <option v-for="color in colors" :key="'hair-' + color" :value="color">
                {{ color }}
              </option>
            </select>
          </div>
        </div>

        <div class="form-row">
          <div class="form-group">
            <label for="height">Height</label>
            <input
                id="height"
                v-model.number="form.height"
                type="number"
                min="1"
                placeholder="Enter height"
            >
          </div>

          <div class="form-group">
            <label for="weight">Weight *</label>
            <input
                id="weight"
                v-model.number="form.weight"
                type="number"
                min="1"
                required
                placeholder="Enter weight"
            >
          </div>
        </div>

        <div class="form-group">
          <label for="nationality">Nationality *</label>
          <select id="nationality" v-model="form.nationality" required>
            <option value="">Select nationality</option>
            <option v-for="country in countries" :key="country" :value="country">
              {{ country }}
            </option>
          </select>
        </div>

        <div class="form-group">
          <label for="birthday">Birthday *</label>
          <input
              id="birthday"
              v-model="form.birthday"
              type="datetime-local"
              required
          >
        </div>

        <fieldset class="coordinates-fieldset">
          <legend>Coordinates *</legend>
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
                  placeholder="Enter X coordinate"
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
                  placeholder="Enter Y coordinate"
              >
            </div>
          </div>
        </fieldset>

        <fieldset class="location-fieldset">
          <legend>Location *</legend>
          <div class="form-row">
            <div class="form-group">
              <label for="locX">X</label>
              <input
                  id="locX"
                  v-model.number="form.location.x"
                  type="number"
                  step="0.1"
                  required
                  placeholder="Enter location X"
              >
            </div>

            <div class="form-group">
              <label for="locY">Y</label>
              <input
                  id="locY"
                  v-model.number="form.location.y"
                  type="number"
                  required
                  placeholder="Enter location Y"
              >
            </div>
          </div>

          <div class="form-group">
            <label for="locName">Location Name</label>
            <input
                id="locName"
                v-model="form.location.name"
                type="text"
                required
                placeholder="Enter location name"
            >
          </div>
        </fieldset>

        <div class="form-actions">
          <button type="button" class="btn btn-secondary" @click="$emit('cancel')">
            Cancel
          </button>
          <button type="submit" class="btn btn-primary">
            {{ editingPerson ? 'Update' : 'Create' }}
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
          // Преобразуем дату для datetime-local input
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
      // Валидация
      if (!this.validateForm()) {
        return
      }

      // Преобразуем данные перед отправкой
      const personData = {
        ...this.form,
        birthday: new Date(this.form.birthday).toISOString()
      }

      this.$emit('save', personData)
    },

    validateForm() {
      if (this.form.coordinates.x <= -860) {
        alert('X coordinate must be greater than -860')
        return false
      }

      if (this.form.coordinates.y > 396) {
        alert('Y coordinate cannot be greater than 396')
        return false
      }

      if (this.form.height !== null && this.form.height <= 0) {
        alert('Height must be greater than 0')
        return false
      }

      if (this.form.weight <= 0) {
        alert('Weight must be greater than 0')
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
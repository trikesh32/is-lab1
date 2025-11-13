<template>
  <div class="operations-panel">
    <h3>Специальные операции</h3>

    <div class="operations-grid">
      <div class="operation-card">
        <h4>Общий рост</h4>
        <button class="btn btn-info" @click="$emit('calculate-total-height')">
          Вычислить
        </button>
      </div>

      <div class="operation-card">
        <h4>Подсчет по весу</h4>
        <div class="operation-input">
          <input
              v-model.number="weightFilter"
              type="number"
              min="1"
              placeholder="Введите вес"
          >
          <button
              class="btn btn-info"
              @click="$emit('count-by-weight', weightFilter)"
              :disabled="!weightFilter"
          >
            Подсчитать
          </button>
        </div>
      </div>

      <div class="operation-card">
        <h4>Поиск по дню рождения</h4>
        <div class="operation-input">
          <input
              v-model="birthdayFilter"
              type="datetime-local"
          >
          <button
              class="btn btn-info"
              @click="$emit('find-by-birthday', birthdayFilter)"
              :disabled="!birthdayFilter"
          >
            Найти
          </button>
        </div>
      </div>

      <div class="operation-card">
        <h4>Процент цвета волос</h4>
        <div class="operation-input">
          <select v-model="selectedHairColor">
            <option value="">Выберите цвет</option>
            <option v-for="color in colors" :key="'hair-op-' + color" :value="color">
              {{ color }}
            </option>
          </select>
          <button
              class="btn btn-info"
              @click="$emit('hair-color-percentage', selectedHairColor)"
              :disabled="!selectedHairColor"
          >
            Вычислить
          </button>
        </div>
      </div>

      <div class="operation-card">
        <h4>Процент цвета глаз</h4>
        <div class="operation-input">
          <select v-model="selectedEyeColor">
            <option value="">Выберите цвет</option>
            <option v-for="color in colors" :key="'eye-op-' + color" :value="color">
              {{ color }}
            </option>
          </select>
          <button
              class="btn btn-info"
              @click="$emit('eye-color-percentage', selectedEyeColor)"
              :disabled="!selectedEyeColor"
          >
            Вычислить
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'PersonOperations',
  emits: [
    'calculate-total-height',
    'count-by-weight',
    'find-by-birthday',
    'hair-color-percentage',
    'eye-color-percentage'
  ],
  data() {
    return {
      weightFilter: null,
      birthdayFilter: '',
      selectedHairColor: '',
      selectedEyeColor: '',
      colors: ['BLACK', 'BLUE', 'YELLOW', 'ORANGE', 'WHITE']
    }
  }
}
</script>

<style scoped>
.operations-panel {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
  padding: 1.5rem;
  margin-bottom: 2rem;
}

.operations-panel h3 {
  margin: 0 0 1.5rem 0;
  color: #333;
  border-bottom: 2px solid #007bff;
  padding-bottom: 0.5rem;
}

.operations-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 1rem;
}

.operation-card {
  border: 1px solid #e9ecef;
  border-radius: 6px;
  padding: 1rem;
  background: #f8f9fa;
}

.operation-card h4 {
  margin: 0 0 1rem 0;
  color: #495057;
  font-size: 0.9rem;
  font-weight: 600;
}

.operation-input {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.operation-input input,
.operation-input select {
  padding: 0.5rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 0.9rem;
}

.btn-info {
  background-color: #17a2b8;
  color: white;
}

.btn-info:hover {
  background-color: #138496;
}

.btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

@media (max-width: 768px) {
  .operations-grid {
    grid-template-columns: 1fr;
  }
}
</style>
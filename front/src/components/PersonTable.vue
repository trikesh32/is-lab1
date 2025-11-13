<template>
  <div class="person-table">
    <div class="table-header">
      <h2>Люди</h2>
      <div class="table-actions">
        <button class="btn btn-primary" @click="$emit('add-person')">
          Добавить
        </button>
        <button class="btn btn-secondary" @click="$emit('refresh')">
          Обновить
        </button>
      </div>
    </div>

    <div class="table-container">
      <table class="table">
        <thead>
        <tr>
          <th>ID</th>
          <th>Имя</th>
          <th>Цвет глаз</th>
          <th>Цвет волос</th>
          <th>Рост</th>
          <th>Вес</th>
          <th>Национальность</th>
          <th>Действия</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="person in persons" :key="person.id">
          <td>{{ person.id }}</td>
          <td>{{ person.name }}</td>
          <td>{{ person.eyeColor }}</td>
          <td>{{ person.hairColor }}</td>
          <td>{{ person.height || 'N/A' }}</td>
          <td>{{ person.weight }}</td>
          <td>{{ person.nationality }}</td>
          <td class="actions">
            <button class="btn btn-sm btn-primary" @click="$emit('edit-person', person)">
              Изменить
            </button>
            <button class="btn btn-sm btn-danger" @click="$emit('delete-person', person)">
              Удалить
            </button>
          </td>
        </tr>
        <tr v-if="persons.length === 0">
          <td colspan="8" class="no-data">Люди не найдены</td>
        </tr>
        </tbody>
      </table>
    </div>

    <div class="pagination" v-if="pagination.totalPages > 1">
      <button
          class="btn btn-sm"
          :disabled="pagination.page === 0"
          @click="$emit('page-change', pagination.page - 1)"
      >
        Назад
      </button>

      <span class="page-info">
        Страница {{ pagination.page + 1 }} из {{ pagination.totalPages }}
        (Всего: {{ pagination.totalElements }})
      </span>

      <button
          class="btn btn-sm"
          :disabled="pagination.page === pagination.totalPages - 1"
          @click="$emit('page-change', pagination.page + 1)"
      >
        Вперед
      </button>
    </div>
  </div>
</template>

<script>
export default {
  name: 'PersonTable',
  props: {
    persons: {
      type: Array,
      required: true
    },
    pagination: {
      type: Object,
      required: true
    }
  },
  emits: ['page-change', 'edit-person', 'delete-person', 'add-person', 'refresh']
}
</script>

<style scoped>
.person-table {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
  margin-bottom: 2rem;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem;
  border-bottom: 1px solid #eee;
}

.table-header h2 {
  margin: 0;
  color: #333;
}

.table-actions {
  display: flex;
  gap: 0.5rem;
}

.table-container {
  overflow-x: auto;
}

.table {
  width: 100%;
  border-collapse: collapse;
}

.table th,
.table td {
  padding: 1rem;
  text-align: left;
  border-bottom: 1px solid #eee;
}

.table th {
  background-color: #f8f9fa;
  font-weight: 600;
  color: #495057;
}

.table tbody tr:hover {
  background-color: #f8f9fa;
}

.actions {
  display: flex;
  gap: 0.5rem;
}

.btn-sm {
  padding: 0.25rem 0.5rem;
  font-size: 0.8rem;
}

.no-data {
  text-align: center;
  color: #6c757d;
  font-style: italic;
}

.pagination {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 1.5rem;
  border-top: 1px solid #eee;
}

.page-info {
  color: #6c757d;
  font-size: 0.9rem;
}

.btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-secondary {
  background-color: #6c757d;
  color: white;
}

.btn-secondary:hover {
  background-color: #545b62;
}
</style>
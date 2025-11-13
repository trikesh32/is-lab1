<template>
  <div class="import-upload">
    <div class="upload-card">
      <h2>📤 Import Persons from CSV</h2>

      <div class="file-input-wrapper">
        <input
          type="file"
          ref="fileInput"
          @change="onFileSelected"
          accept=".csv"
          id="file-upload"
        />
        <label for="file-upload" class="file-label">
          <span v-if="!selectedFile">Choose CSV file</span>
          <span v-else>{{ selectedFile.name }}</span>
        </label>
      </div>

      <button
        @click="handleFileUpload"
        :disabled="!selectedFile || uploading"
        class="btn btn-primary upload-btn"
      >
        <span v-if="!uploading">Upload & Import</span>
        <span v-else>Uploading...</span>
      </button>

      <div v-if="uploadResult" class="upload-result" :class="uploadResult.type">
        <p>{{ uploadResult.message }}</p>
        <div v-if="uploadResult.details">
          <p><strong>Import ID:</strong> {{ uploadResult.details.id }}</p>
          <p><strong>Status:</strong> {{ uploadResult.details.status }}</p>
          <p v-if="uploadResult.details.objectsCount">
            <strong>Objects imported:</strong> {{ uploadResult.details.objectsCount }}
          </p>
        </div>
      </div>

      <div class="csv-format-info">
        <h3>CSV Format:</h3>
        <p>The CSV file should have the following columns (in order):</p>
        <code>name,coord_x,coord_y,eyeColor,hairColor,loc_x,loc_y,loc_name,height,birthday,weight,nationality</code>
        <p class="format-note">
          <strong>Example:</strong><br>
          John Doe,100.5,200.3,BLUE,BLACK,50.0,100,Moscow,180,2000-01-15T10:00:00+03:00,75,RUSSIA
        </p>
        <p class="format-note">
          <strong>Valid colors:</strong> BLACK, BLUE, YELLOW, ORANGE, WHITE<br>
          <strong>Valid countries:</strong> RUSSIA, SPAIN, THAILAND
        </p>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'ImportUpload',
  data() {
    return {
      selectedFile: null,
      uploading: false,
      uploadResult: null
    }
  },
  methods: {
    onFileSelected(event) {
      this.selectedFile = event.target.files[0]
      this.uploadResult = null
    },
    
    async handleFileUpload() {
      if (!this.selectedFile) {
        this.uploadResult = {
          type: 'error',
          message: 'Please select a file'
        }
        return
      }

      this.uploading = true
      this.uploadResult = null

      const formData = new FormData()
      formData.append('file', this.selectedFile)

      // Получаем токен из localStorage
      const token = localStorage.getItem('token')

      try {
        const response = await axios.post('http://localhost:8080/api/import/upload', formData, {
          headers: {
            'Content-Type': 'multipart/form-data',
            'Authorization': `Bearer ${token}`
          }
        })

        this.uploadResult = {
          type: 'success',
          message: 'File uploaded and imported successfully!',
          details: response.data
        }

        // Очищаем форму
        this.selectedFile = null
        this.$refs.fileInput.value = ''

        // Уведомляем родительский компонент
        this.$emit('import-completed', response.data)

      } catch (error) {
        this.uploadResult = {
          type: 'error',
          message: error.response?.data?.error || error.message || 'Upload failed'
        }
      } finally {
        this.uploading = false
      }
    }
  }
}
</script>

<style scoped>
.import-upload {
  margin: 2rem 0;
}

.upload-card {
  background: white;
  border-radius: 8px;
  padding: 2rem;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.upload-card h2 {
  margin-bottom: 1.5rem;
  color: #333;
}

.file-input-wrapper {
  margin-bottom: 1.5rem;
}

.file-input-wrapper input[type="file"] {
  display: none;
}

.file-label {
  display: inline-block;
  padding: 0.75rem 1.5rem;
  background-color: #f0f0f0;
  border: 2px dashed #ccc;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s ease;
  width: 100%;
  text-align: center;
}

.file-label:hover {
  background-color: #e0e0e0;
  border-color: #999;
}

.upload-btn {
  width: 100%;
  padding: 1rem;
  font-size: 1.1rem;
  font-weight: 500;
}

.upload-btn:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}

.upload-result {
  margin-top: 1.5rem;
  padding: 1rem;
  border-radius: 4px;
}

.upload-result.success {
  background-color: #d4edda;
  border: 1px solid #c3e6cb;
  color: #155724;
}

.upload-result.error {
  background-color: #f8d7da;
  border: 1px solid #f5c6cb;
  color: #721c24;
}

.upload-result p {
  margin: 0.5rem 0;
}

.csv-format-info {
  margin-top: 2rem;
  padding: 1.5rem;
  background-color: #f8f9fa;
  border-radius: 4px;
  border-left: 4px solid #007bff;
}

.csv-format-info h3 {
  margin-top: 0;
  color: #007bff;
}

.csv-format-info code {
  display: block;
  padding: 0.75rem;
  background-color: #e9ecef;
  border-radius: 4px;
  margin: 0.5rem 0;
  overflow-x: auto;
  font-size: 0.9rem;
}

.format-note {
  margin-top: 1rem;
  font-size: 0.9rem;
  color: #666;
}
</style>
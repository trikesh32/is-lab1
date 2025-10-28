import SockJS from 'sockjs-client'
import { Client } from '@stomp/stompjs'

class WebSocketService {
    constructor() {
        this.stompClient = null
        this.connected = false
        this.subscriptions = new Map()
    }

    connect(onMessage, onError) {
        const socket = new SockJS('http://localhost:8080/ws')
        this.stompClient = new Client({
            webSocketFactory: () => socket,
            debug: (str) => {
                console.log('STOMP: ' + str)
            },
            reconnectDelay: 5000,
            heartbeatIncoming: 4000,
            heartbeatOutgoing: 4000,
        })

        this.stompClient.onConnect = (frame) => {
            console.log('Connected: ' + frame)
            this.connected = true

            // Подписываемся на обновления persons
            const subscription = this.stompClient.subscribe('/topic/persons', (message) => {
                const wsMessage = JSON.parse(message.body)
                onMessage(wsMessage)
            })

            this.subscriptions.set('persons', subscription)
        }

        this.stompClient.onStompError = (frame) => {
            console.error('Broker reported error: ' + frame.headers['message'])
            console.error('Additional details: ' + frame.body)
            if (onError) onError(frame)
        }

        this.stompClient.onWebSocketError = (error) => {
            console.error('WebSocket error:', error)
            if (onError) onError(error)
        }

        this.stompClient.activate()
    }

    disconnect() {
        if (this.stompClient) {
            // Отписываемся от всех подписок
            this.subscriptions.forEach((subscription, key) => {
                subscription.unsubscribe()
            })
            this.subscriptions.clear()

            this.stompClient.deactivate()
            this.connected = false
            console.log('WebSocket disconnected')
        }
    }

    sendMessage(destination, message) {
        if (this.connected && this.stompClient) {
            this.stompClient.publish({
                destination: destination,
                body: JSON.stringify(message)
            })
        }
    }
}

export default new WebSocketService()
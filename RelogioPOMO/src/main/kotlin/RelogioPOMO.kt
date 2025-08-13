import javax.swing.*
import java.awt.*
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.util.Timer
import java.util.TimerTask
import java.text.SimpleDateFormat
import java.util.Date
import java.io.File
import java.io.FileWriter
import java.io.BufferedReader
import java.io.FileReader

class PomodoroApp : JFrame() {
    private val workTime: Long = 25 * 60 * 1000 // 25 minutos em milissegundos
    private val breakTime: Long = 5 * 60 * 1000 // 5 minutos em milissegundos
    private var remainingTime: Long = workTime
    private var isWorking = true
    private var isPaused = false
    private var timer: Timer? = null
    private var fontSize: Int = 150

    private val timeLabel: JLabel = JLabel("25:00", SwingConstants.CENTER)
    private val startButton: JButton = JButton("Iniciar")
    private val pauseButton: JButton = JButton("Pausar")
    private val resetButton: JButton = JButton("Resetar")
    private val messageLabel: JLabel = JLabel("", SwingConstants.CENTER)
    private val increaseFontButton: JButton = JButton("Aumentar")
    private val decreaseFontButton: JButton = JButton("Diminuir")
    private val viewHistoryButton: JButton = JButton("Ver Histórico")

    init {
        setupFrame()
        setupButtons()
    }

    private fun setupFrame() {
        title = "Pomodoro Timer" //Define o título da janela
        setSize(800, 600) // Define o tamanho inicial da janela
        setLocationRelativeTo(null)// Centraliza a janela na tela
        defaultCloseOperation = JFrame.EXIT_ON_CLOSE// Define que ao fechar a janela, o programa termina
        layout = BorderLayout() // Define o layout principal da janela

        timeLabel.font = Font("Arial", Font.BOLD, fontSize) //  É o rótulo que exibe o tempo restante (por exemplo, "25:00")
        timeLabel.foreground = Color.WHITE //Define a cor do texto (branco).
        timeLabel.background = Color.BLACK// Define a cor de fundo (preto).
        timeLabel.isOpaque = true//  Garante que a cor de fundo seja visível.

        // CONFIG BARRA DE MENSAGEM NO TOPO DO PROGRAMA
        messageLabel.font = Font("Arial", Font.ITALIC, 20)
        messageLabel.foreground = Color.GREEN
        messageLabel.background = Color.BLACK
        messageLabel.isOpaque = true

        val topPanel = JPanel(BorderLayout())// Cria um painel para organizar os botões
        topPanel.add(messageLabel, BorderLayout.SOUTH)

        add(timeLabel, BorderLayout.CENTER)
        add(topPanel, BorderLayout.NORTH)

        val panel = JPanel()
        panel.layout = FlowLayout()  // Organiza os botões horizontalmente

        // AQUI ESTA ADICIONADO TODOS OS BOTOES QUE TEMOS NO MENU ABAIXO DO TIMER
        panel.add(startButton)// Adiciona o botão "Iniciar"
        panel.add(pauseButton) // Adiciona o botão "Pausar"
        panel.add(resetButton)
        panel.add(increaseFontButton)// Adiciona o botão "Aumentar"
        panel.add(decreaseFontButton)
        panel.add(viewHistoryButton)// Adiciona o botão "Ver Histórico"

        add(panel, BorderLayout.SOUTH) // Adiciona o painel de botões na parte inferior da tela

        contentPane.background = Color.BLACK
        isVisible = true
        isAlwaysOnTop = true// // Garante que a janela fique sempre visível
    }

    private fun setupButtons() {
        startButton.addActionListener {
            startTimer()
        } // inicia o temporizador

        pauseButton.addActionListener {
            togglePause()
        }// Alterna entre pausar e retomar o temporizador

        resetButton.addActionListener {
            resetTimer()
        }// Reseta o temporizador para o estado inicial

        increaseFontButton.addActionListener {
            fontSize += 10
            timeLabel.font = Font("Arial", Font.BOLD, fontSize)
        }// incrementa o tamanho da fonte em 10 unidades

        decreaseFontButton.addActionListener {
            fontSize -= 10
            timeLabel.font = Font("Arial", Font.BOLD, fontSize)
        }// Reduz o tamanho da fonte em 10 unidades.

        viewHistoryButton.addActionListener {
            showStudyHistory()
        }// Exibe o histórico de estudos, que provavelmente está armazenado localmente
    }

    private fun startTimer() {
        if (timer == null) {
            messageLabel.text = "Cronômetro iniciado!"
            timer = Timer()
            timer?.scheduleAtFixedRate(createTask(), 0, 1000)
        }
    }

    private fun togglePause() {
        if (isPaused) {
            // Retomar
            startTimer()
            pauseButton.text = "Pausar"
            messageLabel.text = "Cronômetro retomado!"
            isPaused = false
        } else {
            // Pausar
            timer?.cancel()
            timer = null
            pauseButton.text = "Retomar"
            messageLabel.text = "Cronômetro pausado!"
            isPaused = true
        }
    }

    private fun resetTimer() {
        timer?.cancel()
        timer = null
        remainingTime = workTime
        isWorking = true
        isPaused = false
        pauseButton.text = "Pausar"
        updateTimeLabel()
        messageLabel.text = "Cronômetro resetado!"
    }

    private fun createTask(): TimerTask {
        return object : TimerTask() {
            override fun run() {
                if (remainingTime > 0) {
                    remainingTime -= 1000
                    updateTimeLabel()
                    if (remainingTime <= 10 * 1000) {
                        Toolkit.getDefaultToolkit().beep()
                    }
                } else {
                    timer?.cancel()
                    timer = null
                    handleCycleEnd()
                }
            }
        }
    }

    private fun handleCycleEnd() {
        if (isWorking) {
            remainingTime = breakTime
            isWorking = false
            messageLabel.text = "Ciclo de trabalho concluído! Hora do descanso."
        } else {
            remainingTime = workTime
            isWorking = true
            messageLabel.text = "Descanso concluído! Vamos trabalhar novamente."
        }
        Toolkit.getDefaultToolkit().beep()
        startTimer()
        storeStudyTime()
    }

    private fun updateTimeLabel() {
        val minutes = remainingTime / 60000
        val seconds = (remainingTime % 60000) / 1000
        timeLabel.text = String.format("%02d:%02d", minutes, seconds)
    }

    private fun storeStudyTime() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val currentDate = dateFormat.format(Date())
        val file = File("study_time.txt")
        val writer = FileWriter(file, true)

        writer.append("Data: $currentDate - Ciclo concluído!\n")
        writer.close()
    }

    private fun showStudyHistory() {
        val file = File("study_time.txt")
        if (file.exists()) {
            val reader = BufferedReader(FileReader(file))
            val history = reader.readLines().joinToString("\n")
            reader.close()

            JOptionPane.showMessageDialog(this, history, "Histórico de Estudo", JOptionPane.INFORMATION_MESSAGE)
        } else {
            JOptionPane.showMessageDialog(
                this,
                "Nenhum histórico encontrado.",
                "Histórico de Estudo",
                JOptionPane.WARNING_MESSAGE
            )
        }
    }
}

fun main() {
    PomodoroApp()
}

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.io.Serializable;
import java.util.List;
import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.*;


class ExamForm extends JFrame {
    private User user;
    private JComboBox<String> subjectComboBox;
    private JButton startExamButton;

    public ExamForm(User user) {
        this.user = user;

        setTitle("Online Examination System - Exam");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initializeComponents();
    }

    private void initializeComponents() {
        JPanel panel = new JPanel(new GridLayout(3, 1));

        subjectComboBox = new JComboBox<>(new String[]{"Java", "Python", "General Knowledge"});
        panel.add(subjectComboBox);

        startExamButton = new JButton("Start Exam");
        startExamButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedSubject = (String) subjectComboBox.getSelectedItem();
                List<Question> questions = OnlineExaminationSystem.getQuestionsForSubject(selectedSubject);
                Exam exam = new Exam(120); // 120 seconds time limit for the exam
                for (Question question : questions) {
                    exam.addQuestion(question);
                }
                exam.conductExam(user);
            }
        });
        panel.add(startExamButton);

        add(panel);
    }
}


class Exam {
    private List<Question> questions;
    private int score;
    private List<Question> wrongQuestions;
    private int timeLimit;
    private JLabel timerLabel;
    private JFrame frame;
    private int currentQuestionIndex = 0;
    private ButtonGroup buttonGroup;
    private List<JRadioButton> optionButtons;
    private JLabel questionLabel;

    public Exam(int timeLimit) {
        this.questions = new ArrayList<>();
        this.score = 0;
        this.wrongQuestions = new ArrayList<>();
        this.timeLimit = timeLimit;
        this.optionButtons = new ArrayList<>();
    }

    public void addQuestion(Question question) {
        questions.add(question);
    }

    public void conductExam(User user) {
        frame = new JFrame("Online Examination System");
        frame.setSize(600, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        JPanel examPanel = new JPanel(new BorderLayout());
        JPanel questionPanel = new JPanel(new GridLayout(0, 1));
        JPanel optionsPanel = new JPanel(new GridLayout(0, 1));
        JPanel controlPanel = new JPanel(new FlowLayout());

        JLabel nameLabel = new JLabel("Welcome, " + user.getName() + "!");
        examPanel.add(nameLabel, BorderLayout.NORTH);

        timerLabel = new JLabel("Time Left: " + timeLimit + " seconds");
        examPanel.add(timerLabel, BorderLayout.SOUTH);

        long startTime = System.currentTimeMillis();
        long endTime = startTime + timeLimit * 1000;

        Timer timer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                long now = System.currentTimeMillis();
                long remainingTime = (endTime - now) / 1000;
                if (remainingTime <= 0) {
                    ((Timer) e.getSource()).stop();
                    showResult();
                } else {
                    timerLabel.setText("Time Left: " + remainingTime + " seconds");
                }
            }
        });
        timer.start();

        questionLabel = new JLabel();
        questionPanel.add(questionLabel);

        buttonGroup = new ButtonGroup();
        for (int i = 0; i < 4; i++) {
            JRadioButton radioButton = new JRadioButton();
            optionButtons.add(radioButton);
            optionsPanel.add(radioButton);
            buttonGroup.add(radioButton);
        }

        JButton nextButton = new JButton("Next Question");
        JButton bookmarkButton = new JButton("Bookmark Question");

        nextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (checkAnswer()) {
                    score++;
                }
                currentQuestionIndex++;
                if (currentQuestionIndex < questions.size()) {
                    loadQuestion(currentQuestionIndex);
                } else {
                    showResult();
                    timer.stop();
                }
            }
        });

        bookmarkButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Implement bookmark functionality if needed
            }
        });

        controlPanel.add(nextButton);
        controlPanel.add(bookmarkButton);

        examPanel.add(questionPanel, BorderLayout.CENTER);
        examPanel.add(optionsPanel, BorderLayout.EAST);
        examPanel.add(controlPanel, BorderLayout.SOUTH);

        frame.add(examPanel);
        frame.setVisible(true);

        loadQuestion(currentQuestionIndex);
    }

    private void loadQuestion(int questionIndex) {
        Question question = questions.get(questionIndex);
        questionLabel.setText(question.getQuestionText());
        List<String> options = question.getOptions();
        for (int i = 0; i < optionButtons.size(); i++) {
            optionButtons.get(i).setText(options.get(i));
        }
    }

    private boolean checkAnswer() {
        int selectedOption = -1;
        for (int i = 0; i < optionButtons.size(); i++) {
            if (optionButtons.get(i).isSelected()) {
                selectedOption = i;
                break;
            }
        }
        return questions.get(currentQuestionIndex).checkAnswer(selectedOption);
    }

    private void showResult() {
        frame.getContentPane().removeAll();
        JLabel resultLabel = new JLabel("Exam Completed. Your score is: " + score + " out of " + questions.size());
        resultLabel.setHorizontalAlignment(SwingConstants.CENTER);
        frame.add(resultLabel);
        frame.setSize(400, 200);
        frame.setLocationRelativeTo(null);
        frame.revalidate();
    }
}


class Question implements Serializable {
    private String questionText;
    private List<String> options;
    private int correctAnswerIndex;
    private String subject;

    public Question(String questionText, List<String> options, int correctAnswerIndex, String subject) {
        this.questionText = questionText;
        this.options = options;
        this.correctAnswerIndex = correctAnswerIndex;
        this.subject = subject;
    }

    public String getQuestionText() {
        return questionText;
    }

    public List<String> getOptions() {
        return options;
    }

    public int getCorrectAnswerIndex() {
        return correctAnswerIndex;
    }

    public String getSubject() {
        return subject;
    }

    public boolean checkAnswer(int selectedOption) {
        return selectedOption == correctAnswerIndex;
    }
}

class User implements Serializable {
    private String name;
    private String password;
    private String registrationNumber;

    public User(String name, String password, String registrationNumber) {
        this.name = name;
        this.password = password;
        this.registrationNumber = registrationNumber;
    }

    public String getName() {
        return name;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public boolean checkPassword(String password) {
        return this.password.equals(password);
    }

    public static void saveUserDataToFile(Map<String, User> userDatabase) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("users.dat"))) {
            oos.writeObject(userDatabase);
            System.out.println("User data saved to file.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    public static Map<String, User> loadUserDataFromFile() {
        File file = new File("users.dat");
        if (!file.exists()) {
            System.out.println("No user data file found. Creating a new user database.");
            return new HashMap<>();
        }
        Map<String, User> userDatabase = new HashMap<>();
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("users.dat"))) {
            Object obj = ois.readObject();
            if (obj instanceof Map) {
                userDatabase = (Map<String, User>) obj;
                System.out.println("User data loaded from file.");
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return userDatabase;
    }
}

public class OnlineExaminationSystem {
    private static Map<String, User> userDatabase = new HashMap<>();
    private static Map<String, List<Question>> subjectQuestionBank = new HashMap<>();
    private static List<Question> questionDatabase;

    public static void main(String[] args) {
        userDatabase = User.loadUserDataFromFile(); // Load user data from file on startup
        initializeQuestions();

        SwingUtilities.invokeLater(() -> createMainFrame());
    }

    private static void createMainFrame() {
        JFrame frame = new JFrame("Online Examination System");
        frame.setSize(400, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout());

        JPanel panel = new JPanel(new GridLayout(3, 1));

        JButton registerButton = new JButton("Register");
        registerButton.addActionListener(e -> createRegisterForm());
        panel.add(registerButton);

        JButton loginButton = new JButton("Login");
        loginButton.addActionListener(e -> createLoginForm());
        panel.add(loginButton);

        frame.add(panel, BorderLayout.CENTER);
        frame.setVisible(true);
    }

    private static void createRegisterForm() {
        JFrame registerFrame = new JFrame("Register");
        registerFrame.setSize(300, 200);
        registerFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        registerFrame.setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(4, 2));

        JLabel nameLabel = new JLabel("Name:");
        JTextField nameField = new JTextField();
        panel.add(nameLabel);
        panel.add(nameField);

        JLabel passwordLabel = new JLabel("Password:");
        JPasswordField passwordField = new JPasswordField();
        panel.add(passwordLabel);
        panel.add(passwordField);

        JLabel regNumberLabel = new JLabel("Registration Number:");
        JTextField regNumberField = new JTextField();
        panel.add(regNumberLabel);
        panel.add(regNumberField);

        JButton registerButton = new JButton("Register");
        registerButton.addActionListener(e -> {
            String name = nameField.getText();
            String password = new String(passwordField.getPassword());
            String registrationNumber = regNumberField.getText();
            if (!name.isEmpty() && !password.isEmpty() && !registrationNumber.isEmpty()) {
                if (!userDatabase.containsKey(registrationNumber)) {
                    User newUser = new User(name, password, registrationNumber);
                    userDatabase.put(registrationNumber, newUser);
                    User.saveUserDataToFile(userDatabase); // Save user data to file on registration
                    JOptionPane.showMessageDialog(registerFrame, "Registration successful!");
                    registerFrame.dispose();
                } else {
                    JOptionPane.showMessageDialog(registerFrame, "Registration number already exists.");
                }
            } else {
                JOptionPane.showMessageDialog(registerFrame, "All fields are required.");
            }
        });
        panel.add(registerButton);

        registerFrame.add(panel);
        registerFrame.setVisible(true);
    }

    private static void createLoginForm() {
        JFrame loginFrame = new JFrame("Login");
        loginFrame.setSize(300, 200);
        loginFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        loginFrame.setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(3, 2));

        JLabel regNumberLabel = new JLabel("Registration Number:");
        JTextField regNumberField = new JTextField();
        panel.add(regNumberLabel);
        panel.add(regNumberField);

        JLabel passwordLabel = new JLabel("Password:");
        JPasswordField passwordField = new JPasswordField();
        panel.add(passwordLabel);
        panel.add(passwordField);

        JButton loginButton = new JButton("Login");
        loginButton.addActionListener(e -> {
            String registrationNumber = regNumberField.getText();
            String password = new String(passwordField.getPassword());
            User user = userDatabase.get(registrationNumber);
            if (user != null && user.checkPassword(password)) {
                JOptionPane.showMessageDialog(loginFrame, "Login successful!");
                loginFrame.dispose();
                showExamForm(user);
            } else {
                JOptionPane.showMessageDialog(loginFrame, "Invalid registration number or password.");
            }
        });
        panel.add(loginButton);

        loginFrame.add(panel);
        loginFrame.setVisible(true);
    }

    private static void showExamForm(User user) {
        ExamForm examForm = new ExamForm(user);
        examForm.setVisible(true);
    }

    private static void initializeQuestions() {
        List<Question> javaQuestions = new ArrayList<>();
        List<Question> pythonQuestions = new ArrayList<>();
        List<Question> generalKnowledgeQuestions = new ArrayList<>();

        // Add Java questions
        javaQuestions.add(new Question("What is the size of int in Java?", Arrays.asList("16 bits", "32 bits", "64 bits", "128 bits"), 1, "Java"));
        javaQuestions.add(new Question("Which of the following is not a Java feature?", Arrays.asList("Object-oriented", "Use of pointers", "Portable", "Dynamic"), 1, "Java"));

        // Add Python questions
        pythonQuestions.add(new Question("What is the output of print(2 ** 3)?", Arrays.asList("6", "8", "9", "4"), 1, "Python"));
        pythonQuestions.add(new Question("Which of the following is not a keyword in Python?", Arrays.asList("pass", "eval", "assert", "nonlocal"), 1, "Python"));

        // Add General Knowledge questions
        generalKnowledgeQuestions.add(new Question("Who is the current president of the United States?", Arrays.asList("Barack Obama", "Donald Trump", "Joe Biden", "George Bush"), 2, "General Knowledge"));
        generalKnowledgeQuestions.add(new Question("What is the capital of France?", Arrays.asList("Berlin", "Madrid", "Rome", "Paris"), 3, "General Knowledge"));

        subjectQuestionBank.put("Java", javaQuestions);
        subjectQuestionBank.put("Python", pythonQuestions);
        subjectQuestionBank.put("General Knowledge", generalKnowledgeQuestions);
    }

    public static List<Question> getQuestionsForSubject(String subject) {
        return subjectQuestionBank.getOrDefault(subject, new ArrayList<>());
    }
}

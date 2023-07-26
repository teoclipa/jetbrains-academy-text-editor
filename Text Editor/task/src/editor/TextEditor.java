package editor;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.nio.file.Files;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextEditor extends JFrame {
    private JTextArea textArea;
    private JTextField searchField;
    private JCheckBox regexCheckBox;
    private final JFileChooser fileChooser;
    private final List<Integer> matchStartIndexes;
    private final List<Integer> matchEndIndexes;
    private int currentMatchIndex;

    public TextEditor() {
        matchStartIndexes = new ArrayList<>();
        matchEndIndexes = new ArrayList<>();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 500);
        setTitle("Text Editor");

        fileChooser = new JFileChooser();
        fileChooser.setName("FileChooser");
        add(fileChooser);
        fileChooser.setVisible(false);


        initializeUI();


        setVisible(true);
    }

    private void initializeUI() {
        createMenuBar();

        textArea = new JTextArea();
        textArea.setName("TextArea");
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setName("ScrollPane");
        add(scrollPane, BorderLayout.CENTER);

        createTopPanel();
        createBottomPanel();
    }

    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu menuFile = new JMenu("File");
        menuFile.setName("MenuFile");
        menuBar.add(menuFile);

        JMenuItem menuOpen = new JMenuItem("Open");
        menuOpen.setName("MenuOpen");
        menuOpen.addActionListener(event -> loadFile());
        menuFile.add(menuOpen);

        JMenuItem menuSave = new JMenuItem("Save");
        menuSave.setName("MenuSave");
        menuSave.addActionListener(event -> saveFile());
        menuFile.add(menuSave);

        JMenuItem menuExit = new JMenuItem("Exit");
        menuExit.setName("MenuExit");
        menuExit.addActionListener(event -> dispose());
        menuFile.add(menuExit);

        JMenu menuSearch = new JMenu("Search");
        menuSearch.setName("MenuSearch");
        menuBar.add(menuSearch);

        JMenuItem menuStartSearch = new JMenuItem("Start Search");
        menuStartSearch.setName("MenuStartSearch");
        menuStartSearch.addActionListener(event -> startSearch());
        menuSearch.add(menuStartSearch);

        JMenuItem menuPreviousMatch = new JMenuItem("Previous Match");
        menuPreviousMatch.setName("MenuPreviousMatch");
        menuPreviousMatch.addActionListener(event -> findPreviousMatch());
        menuSearch.add(menuPreviousMatch);

        JMenuItem menuNextMatch = new JMenuItem("Next Match");
        menuNextMatch.setName("MenuNextMatch");
        menuNextMatch.addActionListener(event -> findNextMatch());
        menuSearch.add(menuNextMatch);

        JCheckBoxMenuItem useRegExpMenuItem = new JCheckBoxMenuItem("Use Regular Expressions");
        useRegExpMenuItem.setName("MenuUseRegExp");
        useRegExpMenuItem.addActionListener(event -> regexCheckBox.setSelected(useRegExpMenuItem.isSelected()));
        menuSearch.add(useRegExpMenuItem);


        setJMenuBar(menuBar);
    }

    private void createTopPanel() {
        JPanel topPanel = new JPanel();

        JButton openButton = new JButton();
        openButton.setName("OpenButton");
        openButton.addActionListener(event -> loadFile());
        topPanel.add(openButton);

        JButton saveButton = new JButton();
        saveButton.setName("SaveButton");
        saveButton.addActionListener(event -> saveFile());
        topPanel.add(saveButton);

        add(topPanel, BorderLayout.NORTH);
    }

    private void createBottomPanel() {
        JPanel bottomPanel = new JPanel();

        searchField = new JTextField(15);
        searchField.setName("SearchField");
        bottomPanel.add(searchField);

        JButton startSearchButton = new JButton();
        startSearchButton.setName("StartSearchButton");
        startSearchButton.addActionListener(event -> startSearch());
        bottomPanel.add(startSearchButton);

        JButton previousMatchButton = new JButton();
        previousMatchButton.setName("PreviousMatchButton");
        previousMatchButton.addActionListener(event -> findPreviousMatch());
        bottomPanel.add(previousMatchButton);

        JButton nextMatchButton = new JButton();
        nextMatchButton.setName("NextMatchButton");
        nextMatchButton.addActionListener(event -> findNextMatch());
        bottomPanel.add(nextMatchButton);

        regexCheckBox = new JCheckBox("Use regex");
        regexCheckBox.setName("UseRegExCheckbox");
        bottomPanel.add(regexCheckBox);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void loadFile() {
        fileChooser.setVisible(true);
        int returnVal = fileChooser.showOpenDialog(null);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                textArea.setText(new String(Files.readAllBytes(selectedFile.toPath())));
            } catch (FileNotFoundException e) {
                textArea.setText("");  // Clear the text area when file doesn't exist
            } catch (IOException e) {
                textArea.setText("");
            }
        }
        fileChooser.setVisible(false);
    }


    private void saveFile() {
        fileChooser.setVisible(true);
        int returnVal = fileChooser.showSaveDialog(null);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try (FileWriter writer = new FileWriter(selectedFile)) {
                writer.write(textArea.getText());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        fileChooser.setVisible(false);
    }
    private void startSearch() {
        matchStartIndexes.clear();
        matchEndIndexes.clear();
        currentMatchIndex = 0;

        String text = textArea.getText();
        String query = searchField.getText();

        Matcher matcher;
        if (regexCheckBox.isSelected()) {
            matcher = Pattern.compile(query).matcher(text);
        } else {
            matcher = Pattern.compile(Pattern.quote(query)).matcher(text);
        }

        while (matcher.find()) {
            matchStartIndexes.add(matcher.start());
            matchEndIndexes.add(matcher.end());
        }

        if (!matchStartIndexes.isEmpty()) {
            selectMatch();
        }
    }

    private void findNextMatch() {
        if (!matchStartIndexes.isEmpty()) {
            currentMatchIndex = (currentMatchIndex + 1) % matchStartIndexes.size();
            selectMatch();
        }
    }

    private void findPreviousMatch() {
        if (!matchStartIndexes.isEmpty()) {
            currentMatchIndex = (currentMatchIndex - 1 + matchStartIndexes.size()) % matchStartIndexes.size();
            selectMatch();
        }
    }

    private void selectMatch() {
        int start = matchStartIndexes.get(currentMatchIndex);
        int end = matchEndIndexes.get(currentMatchIndex);
        textArea.setCaretPosition(end);
        textArea.select(start, end);
        textArea.grabFocus();
    }
}
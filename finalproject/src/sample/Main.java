package sample;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;


public class Main extends Application{
    private Stage stage;
    private Canvas canvas;
    private Group root;
    private GraphicsContext gc;
    private int xDimension = 800;          // starting xDimension of the screen
    private int yDimension = 720;           // starting yDimension of the screen
    private String username = "Player";     // default username
    private String screenDimensions = "1280x720";
    private Image redchip;
    private Image yellowchip;
    private Image empty;
    private Client game;

    private String columnToPlay;

    @Override
    public void start(Stage primaryStage) throws Exception{
        stage = primaryStage;
        stage.setTitle("Connect 4");
        stage.setResizable(false);
        drawMenu();
    }

    public void drawMenu(){
        // create layout for the menu
        BorderPane menu = new BorderPane();

        // create start button
        Button start = new Button("Start");
        start.setPrefWidth(xDimension/6);
        start.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                try {
                    drawGame();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        // create settings button
        Button settings = new Button("Settings");
        settings.setPrefWidth(xDimension/6);
        settings.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                drawSettings();
            }
        });

        Image image = new Image(getClass().getClassLoader().getResource("connect4logo.png").toString());
        ImageView logo = new ImageView(image);
        BorderPane imagePane = new BorderPane();
        imagePane.setCenter(logo);

        // put buttons and image in the layout
        HBox buttonContainer = new HBox();
        buttonContainer.getChildren().addAll(start, settings);
        VBox container = new VBox();
        container.getChildren().addAll(imagePane,buttonContainer);
        buttonContainer.setAlignment(Pos.CENTER);
        container.setAlignment(Pos.CENTER);
        menu.setCenter(container);

        // create scene and show stage
        stage.setScene(new Scene(menu,xDimension,yDimension));
        stage.show();
    }

    public void drawSettings(){
        // create the layout for the settings menu
        VBox settingsMenu = new VBox();

        // create the back button
        Button back = new Button("back to menu");
        back.setPrefWidth(xDimension/6);
        back.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                drawMenu(); // returns to the menu
            }
        });

        // a label to put besides the textfield
        Text name = new Text("Username: ");
        // textfield for the user to enter their new username in
        TextField nameField = new TextField();
        nameField.setPromptText(username);

        // a label to put besides the screen dimensions combo box
        Text dimensions = new Text("Screen Dimensions: ");
        // a combo box that holds different options for resizing the dimensions of the screen
        ComboBox dimensionsMenu = new ComboBox();
        dimensionsMenu.setPromptText(screenDimensions);
        dimensionsMenu.getItems().addAll("1280x720","1366x768","1440x900","1536x864","1920x1080");
        dimensionsMenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                // get selected content from the combo box
                screenDimensions = dimensionsMenu.getSelectionModel().getSelectedItem().toString();
            }
        });

        Button apply = new Button("apply changes");
        apply.setPrefWidth(xDimension/6);
        apply.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                String[] dimensions = screenDimensions.split("x"); // get the x and y dimension
                xDimension = Integer.parseInt(dimensions[0]);
                yDimension = Integer.parseInt(dimensions[1]);
                if(nameField.getText() != null){    // if the user entered a new username update it
                    username = nameField.getText();
                }
                drawSettings(); // redraw the settings menu so the user can see the result of their changes
            }
        });

        // Gridpane to put the Text's, TextFields, and ComboBox in
        GridPane settings = new GridPane();
        settings.setAlignment(Pos.CENTER);
        settings.add(name,0,0,1,1);
        settings.add(nameField,1,0,1,1);
        settings.add(dimensions,0,1,1,1);
        settings.add(dimensionsMenu,1,1,1,1);

        // add everything to the layout
        settingsMenu.getChildren().addAll(back,settings,apply);
        settingsMenu.setAlignment(Pos.CENTER);

        BorderPane boderPane = new BorderPane();
        boderPane.setCenter(settingsMenu);

        stage.setScene(new Scene(boderPane, xDimension, yDimension));
        stage.show();
    }

    public void drawGame() throws IOException {
        //initialize chips
        int chipHeight = (int)((0.9 * yDimension) / 6); // height of the game chips
        redchip = new Image(getClass().getClassLoader().getResource("Redchip.png").toString(),chipHeight-5,chipHeight-5,false,false);
        yellowchip = new Image(getClass().getClassLoader().getResource("yellowchip.png").toString(),chipHeight-5,chipHeight-5,false,false);
        empty = new Image(getClass().getClassLoader().getResource("empty.png").toString(),chipHeight-5,chipHeight-5,false,false);

        // create a layout for the game
        BorderPane graphicsMenu = new BorderPane();
        root = new Group();
        canvas = new Canvas();
        canvas.widthProperty().bind(stage.widthProperty());
        canvas.heightProperty().bind(stage.heightProperty());
        root.getChildren().add(canvas);

        graphicsMenu.setCenter(root);
        Scene scene = new Scene(graphicsMenu, xDimension, yDimension);
        stage.setScene(scene);
        stage.show();
        game = new Client();
        new Thread(game).start();
        drawBoard();
    }

    public void drawBoard() throws IOException {
        int chipHeight = (int)((0.9 * yDimension) / 6); // height of the game chips
        int colHeight = (int)(0.9 * yDimension);        // the height of each column
        int colWidth = (int)((0.9 * xDimension) / 7);   // the width of each column
        int colGap = (int)((0.1 * xDimension) / 8);     // the size of the gap between the columns of the board
        int rowGap = (int)((0.1 * yDimension) / 2);     // the size of the gap between the top and bottom of the game board

        gc = canvas.getGraphicsContext2D();
        // background
//        gc.setFill(Color.BLACK);
//        gc.fillRect(0, 0, xDimension, yDimension);
//        gc.setFill(Color.ROYALBLUE);
//        gc.fillRoundRect(colGap, rowGap, xDimension - 2*colGap, yDimension-2*rowGap,25,25);

        // create event handlers for when the user presses on the gameboard's columns
        Rectangle col1= new Rectangle(colGap,rowGap,colWidth,colHeight);
        col1.setFill(Color.TRANSPARENT);
        col1.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                columnToPlay = "0";
                //redraw(colGap, colWidth, rowGap, chipHeight);
            }
        });

        Rectangle col2 = new Rectangle(colGap*2 + colWidth,rowGap,colWidth,colHeight);
        col2.setFill(Color.TRANSPARENT);
        col2.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                columnToPlay = "1";
                //redraw(colGap, colWidth, rowGap, chipHeight);
            }
        });

        Rectangle col3 = new Rectangle(colGap*3 + colWidth*2,rowGap,colWidth,colHeight);
        col3.setFill(Color.TRANSPARENT);
        col3.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                columnToPlay = "2";
                //redraw(colGap, colWidth, rowGap, chipHeight);
            }
        });

        Rectangle col4 = new Rectangle(colGap*4 + colWidth*3,rowGap,colWidth,colHeight);
        col4.setFill(Color.TRANSPARENT);
        col4.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                columnToPlay = "3";
                //redraw(colGap, colWidth, rowGap, chipHeight);
            }
        });

        Rectangle col5 = new Rectangle(colGap*5 + colWidth*4,rowGap,colWidth,colHeight);
        col5.setFill(Color.TRANSPARENT);
        col5.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                columnToPlay = "4";
                //redraw(colGap, colWidth, rowGap, chipHeight);
            }
        });

        Rectangle col6 = new Rectangle(colGap*6 + colWidth*5,rowGap,colWidth,colHeight);
        col6.setFill(Color.TRANSPARENT);
        col6.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                columnToPlay = "5";
                //redraw(colGap, colWidth, rowGap, chipHeight);
            }
        });

        Rectangle col7 = new Rectangle(colGap*7 + colWidth*6,rowGap,colWidth,colHeight);
        col7.setFill(Color.TRANSPARENT);
        col7.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                columnToPlay = "6";
                //redraw(colGap, colWidth, rowGap, chipHeight);
            }
        });

//        for(int i = 0; i < 6; i ++){
//            int row = i;
//            for(int j = 0; j < 7; j++){
//                int col = j;
//                if(game.getBoard()[i][j] == 'x'){
//                    gc.drawImage(redchip,(3+colGap *(j+1)) + (colWidth *j),3+rowGap+ chipHeight*i);
//                }else if(game.getBoard()[i][j] == 'o'){
//                    gc.drawImage(yellowchip,(3+colGap *(j+1)) + (colWidth *j),3+rowGap + chipHeight*i);
//                }else{
//                    gc.drawImage(empty,(3+colGap *(j+1)) + (colWidth *j),3+rowGap + chipHeight*i);
//                }
//            }
//        }

        root.getChildren().addAll(col1,col2,col3,col4,col5,col6,col7);




        Timeline timeline = new Timeline();
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.getKeyFrames().add(new KeyFrame(Duration.millis(100), new EventHandler<ActionEvent>(){
            @Override
            public void handle(ActionEvent event) {
                gc.clearRect(0,0,xDimension,yDimension);

                gc.setFill(Color.BLACK);
                gc.fillRect(0, 0, xDimension, yDimension);
                gc.setFill(Color.ROYALBLUE);
                gc.fillRoundRect(colGap, rowGap, xDimension - 2*colGap, yDimension-2*rowGap,25,25);
                for(int i = 0; i < 6; i ++){
                    int row = i;
                    for(int j = 0; j < 7; j++){
                        int col = j;
                        if(game.getBoard()[i][j] == 'X'){
                            gc.drawImage(redchip,(3+colGap *(j+1)) + (colWidth *j),3+rowGap+ chipHeight*i);
                        }else if(game.getBoard()[i][j] == 'O'){
                            gc.drawImage(yellowchip,(3+colGap *(j+1)) + (colWidth *j),3+rowGap + chipHeight*i);
                        }else{
                            gc.drawImage(empty,(3+colGap *(j+1)) + (colWidth *j),3+rowGap + chipHeight*i);
                        }
                    }
                }
            }
        }));
        timeline.playFromStart();
    }

    public String getColumnToPlay(){
        return columnToPlay;
    }

    public void redraw(int colGap, int colWidth, int rowGap, int chipHeight){
        gc.clearRect(0,0,xDimension,yDimension);
        game.displayBoard();

        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, xDimension, yDimension);
        gc.setFill(Color.ROYALBLUE);
        gc.fillRoundRect(colGap, rowGap, xDimension - 2*colGap, yDimension-2*rowGap,25,25);
        for(int i = 0; i < 6; i ++){
            int row = i;
            for(int j = 0; j < 7; j++){
                int col = j;
                if(game.getBoard()[i][j] == 'X'){
                    gc.drawImage(redchip,(3+colGap *(j+1)) + (colWidth *j),3+rowGap+ chipHeight*i);
                }else if(game.getBoard()[i][j] == 'O'){
                    gc.drawImage(yellowchip,(3+colGap *(j+1)) + (colWidth *j),3+rowGap + chipHeight*i);
                }else{
                    gc.drawImage(empty,(3+colGap *(j+1)) + (colWidth *j),3+rowGap + chipHeight*i);
                }
            }
        }

    }

    public class Client extends Thread {
        char putSymbol ='X';
        private BufferedReader in;
        private PrintWriter out;
        private InputStream fileReader;
        private Socket clientSocket;

        public Client() throws IOException {
            clientSocket = new Socket("localHost",8080);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(clientSocket.getOutputStream(),true);
            fileReader = clientSocket.getInputStream();
            initializeBoard();
        }

        private final int ROWS = 6;
        private final int COLS = 7;
        private char[][] board = new char[ROWS][COLS];

        // declare game pieces
        private final char EMP = '-';
        private final char P1 = 'X';
        private final char P2 = 'O';

        /**
         * initializes the board as empty
         */
        public void initializeBoard(){
            for (int r = 0; r < ROWS; r++) {
                for (int c = 0; c < COLS; c++) {
                    board[r][c] = EMP;
                }
            }
        }

        public void play() throws IOException {
            //Initialize game play
            int playerNum = 1;
            boolean gameWon = false;
            int totalMoves = 0;
            int MAX_MOVES = ROWS * COLS;
            //displayBoard();
            Scanner scan = new Scanner(System.in);
            //Create a game play loop
            while ((totalMoves < MAX_MOVES) && (!gameWon)) {
                boolean validMove = false;
//                int columnToPlay = 0;
                //ask current player for a column until a valid move
                //is chosen
                while (!validMove) {
                    System.out.print("Player ");
                    if (playerNum == 1) {
                        System.out.print("1");
                    } else {
                        System.out.print("2");
                    }
                    System.out.println(" - Select a column (0-" +
                            (COLS-1) + ") to play: ");
//                    columnToPlay = scan.nextInt();

                    while (true){
                        columnToPlay = getColumnToPlay();
//                        System.out.println("in loop");

                        if (columnToPlay != null){
                            validMove = addPiece(Integer.parseInt(columnToPlay));
                            if (!validMove) {
                                System.out.println("Column full. Try again.");
                                columnToPlay = null;
                            }
                            else{
                                columnToPlay = null;
                                break;
                            }
                        }
                    }
              }
                totalMoves++;
                //displayBoard();
                gameWon = checkForWinner(playerNum);
                //Switch Player
                if (!gameWon){
                    switch (playerNum){
                        case 1:
                            playerNum = 2;
                            break;
                        default:
                            playerNum = 1;
                    }
                }
            }
            if (gameWon) {
                System.out.print("Congratualations player: ");
                if (playerNum == 1) {
                    System.out.print("1");
                } else {
                    System.out.print("2");
                }
                System.out.println(". You won");
            }
            else if (totalMoves == MAX_MOVES) {
                System.out.println("Game over, no more moves remaining");
                System.exit(0);
            }

        }

        boolean addPiece(int c) throws IOException {
            //check if column c is full
            if (board[0][c] != EMP) {
                return false;
            } else {
                //add piece to lowest unoccupied slot in column c
                for(int r = ROWS-1; r > -1; r--) {
                    if (board[r][c] == EMP) {
                        out.println(r+","+c);   //
                        board[r][c] = putSymbol;
                        //PlaceInBoard(r,c);

                        //System.out.println("P1" + P1);
                        //putData(r, c,P1);

                        break;
                    }
                }
                return true;
            }
        }


        public char[][] getBoard(){
            return board;
        }

        void displayBoard() {
            System.out.println();
            //Display board one row at a time
            for (int r = 0; r < ROWS; r++) {
                //For each row display all of the columns
                for (int c = 0; c < COLS; c++) {
                    System.out.print(board[r][c] + " ");
                }
                //After each row output newline character
                System.out.println();
            }
        }

        boolean checkForWinner(int playerNum) {
            char currTile = playerNum == 1? P1:P2;

            // horizontal check
            for (int r = 0; r<ROWS ; r++ ){
                for (int c = 0; c<COLS-3; c++){
                    if (this.board[r][c] == currTile && this.board[r][c+1] == currTile && this.board[r][c+2] == currTile && this.board[r][c+3] == currTile){
                        return true;
                    }
                }
            }

            // verticalCheck
            for (int c = 0; c<COLS ; c++ ){
                for (int r = ROWS-1; r>3; r--){
                    if (this.board[r][c] == currTile && this.board[r-1][c] == currTile && this.board[r-2][c] == currTile && this.board[r-3][c] == currTile){
                        return true;
                    }
                }
            }

            // ascendingDiagonalCheck
            /*
             *
             *
             *
             * <-- start here go up
             */
            for (int r=3; r<ROWS; r++){
                for (int c=0; c<COLS-3; c++){
                    if (this.board[r][c] == currTile && this.board[r-1][c+1] == currTile && this.board[r-2][c+2] == currTile && this.board[r-3][c+3] == currTile){
                        return true;
                    }
                }
            }

            // descendingDiagonalCheck
            /*
             * <-- start here go down
             *
             *
             *
             */
            for (int r=0; r<ROWS-3; r++){
                for (int c=0; c<COLS-3; c++){
                    if (this.board[r][c] == currTile && this.board[r+1][c+1] == currTile && this.board[r+2][c+2] == currTile && this.board[r+3][c+3] == currTile) {
                        return true;
                    }
                }
            }
            return false;
        }

        char symbol;
        @Override
        public void run() {
            try {
                CompletableFuture.runAsync(()->{
                    try {
                        play();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });

                String line;
                while((line = in.readLine())!=null){        // row, col, sumbol
                    String response = line;
                    //System.out.println( " s " + response);
                    symbol = response.charAt(3);
                    if(symbol == 'X')
                        putSymbol = 'O';
                    else
                        putSymbol = 'X';
                    putBord(response);
                    //displayBoard();
                }

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    in.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        public void putBord(String pos){
            char row1  = pos.charAt(0);
            char column1 = pos.charAt(2);
            int addRow1 =Character.getNumericValue(row1);
            int addColumn1 = Character.getNumericValue(column1);
            board[addRow1][addColumn1] = pos.charAt(3);
        }

    }

    public static void main(String[] args) {
        launch(args);
    }
}

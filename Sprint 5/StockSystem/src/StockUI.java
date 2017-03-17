import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.Array;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.TimerTask;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

import java.util.Timer;

public class StockUI implements ActionListener
{
    static StockUI reference;
    SystemFacade system;
    JFrame frame;
    JFrame historyFrame;
    JLabel headerLabel;
    JLabel statusLabel;
    JLabel moneyLabel;
    JButton viewStocksButton;
    JButton refreshHistoryButton;
    JButton buyButton;
    JButton sellButton;
    Timer buttonTimer;
    JTextPane log;
    JTable ownedShares;
    JTable stocksTable;
    JTable historyTable;
    JComboBox<String> historyCombo;
    final static int LOG_LINE_LIMIT = 100;
    final static String ACTION_BUY = "ACTION_BUY";
    final static String ACTION_SELL = "ACTION_SELL";
    final static String ACTION_VIEWSTOCKS = "ACTION_VIEWSTOCKS";
    final static String ACTION_CHOOSEHISTORY = "ACTION_CHOOSEHISTORY";
    final static String ACTION_REFRESHHISTORY = "ACTION_REFRESHHISTORY";
    Vector<String> stockColumns;
    Vector<String> sharesColumns;
    Vector<String> historyColumns;

    public static StockUI getInstance(SystemFacade systemFacade)
    {
        if (reference == null) {
            reference = new StockUI(systemFacade);
        }
        return reference;
    }

    private StockUI (SystemFacade facade)
    {
        system = facade;

        // gets created from main so this schedules the UI creation on its own thread
        // use invokelater again when items are to be updated
        SwingUtilities.invokeLater(this::generateUI);
        buttonTimer = new Timer();
    }

    private void generateUI()
    {
        stockColumns = new Vector<>(4);
        stockColumns.add("Name");
        stockColumns.add("Price");
        stockColumns.add("Diff");

        sharesColumns = new Vector<>(3);
        sharesColumns.add("Company");
        sharesColumns.add("Amount owned");
        sharesColumns.add("Value");

        Border blackBorder = BorderFactory.createLineBorder(Color.BLACK);

        // Create and set up the window.
        frame = new JFrame("Soggy Stocks");
        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        frame.setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (JOptionPane.showConfirmDialog(frame,
                        "Are you sure you want to close the system?", "Close",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION){
                    exit();
                }
            }
        });

        /* Top Panel */
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(new EmptyBorder(0, 10, 0, 10));
        headerLabel = new JLabel("ACME Stock System", JLabel.CENTER);
        headerLabel.setFont(new Font(headerLabel.getFont().getName(), Font.BOLD, 24));

        JPanel moneyPanel = new JPanel(new BorderLayout());
        moneyPanel.setBorder(BorderFactory.createTitledBorder(blackBorder, "Current Funds"));
        moneyPanel.setPreferredSize(new Dimension(160, 50));

        moneyLabel = new JLabel("0 GoatCoins  ", SwingConstants.RIGHT);
        moneyPanel.add(moneyLabel, BorderLayout.CENTER);


        topPanel.add(headerLabel, BorderLayout.LINE_START);
        topPanel.add(moneyPanel, BorderLayout.LINE_END);
        /* End Header Row */

        /* Mid Panel (Log) */
        JPanel midPanel = new JPanel(new GridLayout(1,2));
        midPanel.setPreferredSize(new Dimension(1200, 800));

        log = new JTextPane();
        log.setEditable(false);
        //log.setLineWrap(true);
        //log.setWrapStyleWord(true);
        log.setBackground(Color.BLACK);
        log.setForeground(Color.WHITE);
        log.setAutoscrolls(true);

        JScrollPane scroll = new JScrollPane(log);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        JPanel logPanel = new JPanel(new BorderLayout());
        logPanel.setPreferredSize(new Dimension());
        logPanel.add(scroll, BorderLayout.CENTER);

        ownedShares = createTable();
        JScrollPane ownedSharesScroll = new JScrollPane(ownedShares);
        ownedSharesScroll.setBorder(BorderFactory.createTitledBorder(blackBorder, "Share Portfolio"));
        ownedShares.setFillsViewportHeight(true);

        stocksTable = createTable();
        JScrollPane stocksTableScroll = new JScrollPane(stocksTable);
        stocksTableScroll.setBorder(BorderFactory.createTitledBorder(blackBorder, "FTSE 100"));
        stocksTable.setFillsViewportHeight(true);

        JPanel stockPanel = new JPanel(new GridLayout(2, 1));
        stockPanel.add(ownedSharesScroll, BorderLayout.PAGE_START);
        stockPanel.add(stocksTableScroll, BorderLayout.PAGE_END);

        midPanel.add(logPanel);
        midPanel.add(stockPanel);
        /* End Mid Panel */

        /* Bottom Button panel */
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBorder(new EmptyBorder(0, 10, 0, 10));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        viewStocksButton = new JButton("View History");
        viewStocksButton.setActionCommand(ACTION_VIEWSTOCKS);
        viewStocksButton.addActionListener(this);
        buyButton = new JButton("Buy");
        buyButton.setActionCommand(ACTION_BUY);
        buyButton.addActionListener(this);
        sellButton = new JButton("Sell");
        sellButton.setActionCommand(ACTION_SELL);
        sellButton.addActionListener(this);

        buttonPanel.add(viewStocksButton);
        buttonPanel.add(buyButton);
        buttonPanel.add(sellButton);

        statusLabel = new JLabel("Disconnected from server.");

        bottomPanel.add(buttonPanel, BorderLayout.LINE_START);
        bottomPanel.add(statusLabel, BorderLayout.LINE_END);

        /* End Bottom Panel */

        // Add the three panels to the main window
        frame.add(topPanel);
        frame.add(midPanel);
        frame.add(bottomPanel);

        // pack resizes the window to fit its components
        frame.pack();
        frame.setVisible(true); // show window
        frame.setLocationRelativeTo(null); // center screen
    }

    public void exit()
    {
        buttonTimer.cancel();
        system.quit();
        frame.dispose();

        if(historyFrame != null)
            historyFrame.dispose();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String action = e.getActionCommand();

        switch (action) {
            case ACTION_BUY:
                buyCommand();
                break;
            case ACTION_SELL:
                sellCommand();
                break;
            case ACTION_VIEWSTOCKS:
                viewHistoryCommand();
                break;
            case ACTION_CHOOSEHISTORY:
                selectHistoryItem((JComboBox)e.getSource());
                break;
            case ACTION_REFRESHHISTORY:
                if (historyCombo.getItemCount() > 0)
                {
                    selectHistoryItem(historyCombo);
                }
                break;
            default:
        }
    }


    public void printToLog (String message, Boolean error)
    {
        SwingUtilities.invokeLater(() -> {
            print(message, error);
        });
    }

    public void updateAccount(Account account) { SwingUtilities.invokeLater(() -> updateBalance(account.getBalance()));}

    public void notifyConnectionChanged(int connectionStatus)
    {
        SwingUtilities.invokeLater(() -> setStatus(connectionStatus));
    }

    public void updateStocks(ArrayList<Stock> stocks)
    {
        SwingUtilities.invokeLater(() -> updateTable(stocksTable, convertStockToTable(stocks), stockColumns));
    }

    public void updateOwnedShares(ArrayList<Share> shares)
    {
        SwingUtilities.invokeLater(() -> updateTable(ownedShares, convertSharesToTable(shares), sharesColumns));
    }

    private JTable createTable()
    {
        JTable table = new JTable() {
            public boolean isCellEditable(int nRow, int nCol) {
                return false;
            }
        };
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        return table;
    }

    private void updateTable(JTable table, Vector<Vector<Object>> values, Vector<String> columns) {
        DefaultTableModel tableModel = (DefaultTableModel) table.getModel();

        try {
            tableModel.setDataVector(values, columns);
        } catch (IndexOutOfBoundsException iob) {
            iob.printStackTrace();
        }
    }

    private void print (String message, Boolean error)
    {
        Color c;

        if(error) c = Color.red;
        else c = Color.white;

        StyledDocument doc = log.getStyledDocument();
        SimpleAttributeSet attribs = new SimpleAttributeSet();
        StyleConstants.setForeground(attribs, c);

        try
        {
            doc.insertString(doc.getLength(), "\n" + message, attribs);
        }
        catch (Exception e)
        {

        }

        if(error) showError(message);
    }

    private void updateBalance(double balance)
    {
        moneyLabel.setText( String.format( "%.2f GoatCoins  ", balance ) );
    }

    private void setStatus (int status)
    {
        String text;
        switch (status) {
            case SystemFacade.SERVER_CONNECTED:
                text = "Connected to server.";
                break;
            case SystemFacade.SERVER_DISCONNECTED:
                text = "Disconnected from server.";
                break;
            case SystemFacade.SERVER_ERROR:
                text = "Error connecting to server.";
                break;
            default:
                text = "Connecting to server..";
        }

        statusLabel.setText(text);
    }

    private void buyCommand()
    {
        disableButtons();
        // buy button
        // open dialog with drop down
        JComboBox<String> stockCombo;
        try {
            ArrayList<String> names = system.getStockNames();
            String[] comboList = new String[names.size()];
            names.toArray(comboList);
            stockCombo = new JComboBox<>(comboList);
        } catch (NullPointerException ne) {
            stockCombo = new JComboBox<>();
            ne.printStackTrace();
        }

        JPanel myPanel = new JPanel();
        myPanel.add(new JLabel("Company:"));
        myPanel.add(stockCombo);
        myPanel.add(Box.createHorizontalStrut(15)); // a spacer

        // change spinnermodel max to the number of purchasable shares
        SpinnerModel model = new SpinnerNumberModel(1, 1, 100, 1);
        JSpinner spinner = new JSpinner(model);
        myPanel.add(new JLabel("Amount:"));
        myPanel.add(spinner);

        int result = JOptionPane.showConfirmDialog(null, myPanel,
                "Please Select Company and Amount", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            Object name = stockCombo.getSelectedItem();
            Object value = spinner.getValue();

            JPanel myPanel2 = new JPanel();
            myPanel2.add(new JLabel("Are you sure you want to buy these shares?"));

            int result2 = JOptionPane.showConfirmDialog(null, myPanel2,
                    "Are you sure?", JOptionPane.OK_CANCEL_OPTION);

            if (result2 == JOptionPane.OK_OPTION) {
                if (name != null)
                {
                    system.buy(name.toString(), Integer.valueOf(value.toString()));
                }
            }
        }

        buttonTimer.schedule(new enableTask(), 1000);
    }

    private void sellCommand()
    {
        ArrayList<String> names = new ArrayList<String>();

        disableButtons();
        //sell button
        // buy button
        // open dialog with drop down
        // TODO: change to get owned share names
        JComboBox<String> stockCombo;
        try {
            names = system.getShareNames();
            String[] comboList = new String[names.size()];
            names.toArray(comboList);
            stockCombo = new JComboBox<>(comboList);
        } catch (NullPointerException ne) {
            stockCombo = new JComboBox<>();
            ne.printStackTrace();
        }

        JPanel myPanel = new JPanel();
        myPanel.add(new JLabel("Company:"));
        myPanel.add(stockCombo);
        myPanel.add(Box.createHorizontalStrut(15)); // a spacer
        // change spinner model max to number of owned shares
        SpinnerModel model = new SpinnerNumberModel(1, 1, 100, 1);
        JSpinner spinner = new JSpinner(model);
        myPanel.add(new JLabel("Amount:"));
        myPanel.add(spinner);

        if(names.size() <= 0)
        {
            showError("You don't own any stocks");
        }
        else
        {
            int result = JOptionPane.showConfirmDialog(null, myPanel,
                    "Please Select Company and Amount", JOptionPane.OK_CANCEL_OPTION);

            if (result == JOptionPane.OK_OPTION) {
                Object name = stockCombo.getSelectedItem();
                Object value = spinner.getValue();

                JPanel myPanel2 = new JPanel();
                myPanel2.add(new JLabel("Are you sure you want to sell these shares?"));

                int result2 = JOptionPane.showConfirmDialog(null, myPanel2,
                        "Are you sure?", JOptionPane.OK_CANCEL_OPTION);

                if (result2 == JOptionPane.OK_OPTION) {
                    if (name != null)
                    {
                        system.sell(name.toString().toUpperCase(), Integer.valueOf(value.toString()));
                    }
                }
            }
        }
        buttonTimer.schedule(new enableTask(), 1000);
    }

    private void disableButtons() {
        buyButton.setEnabled(false);
        sellButton.setEnabled(false);
    }

    private void viewHistoryCommand()
    {
        if(historyFrame != null && historyFrame.isVisible())
            return;

        historyColumns = new Vector<>(4);
        historyColumns.add("Time");
        historyColumns.add("Price");
        historyColumns.add("Diff");

        // view stocks button
        // show new window with history
        historyFrame = new JFrame("Stock History");
        historyFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        historyFrame.setLayout(new BoxLayout(historyFrame.getContentPane(), BoxLayout.Y_AXIS));

        /* Top Panel */
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(new EmptyBorder(0, 10, 0, 10));


        try {
            ArrayList<String> names = system.getStockNames();
            String[] comboList = new String[names.size()];
            names.toArray(comboList);
            historyCombo = new JComboBox<>(comboList);
        } catch (NullPointerException ne) {
            historyCombo = new JComboBox<>();
            ne.printStackTrace();
        }

        if (historyCombo.getItemCount() > 0) {
            historyCombo.setSelectedIndex(0);
            historyCombo.setActionCommand(ACTION_CHOOSEHISTORY);
            historyCombo.addActionListener(this);
        }

        refreshHistoryButton = new JButton("Refresh");
        refreshHistoryButton.setActionCommand(ACTION_REFRESHHISTORY);
        refreshHistoryButton.addActionListener(this);

        topPanel.add(historyCombo, BorderLayout.LINE_START);
        topPanel.add(refreshHistoryButton, BorderLayout.LINE_END);
        /* End Header Row */

        /* Bottom Panel */
        JPanel bottomPanel = new JPanel(new GridLayout(1,1));
        bottomPanel.setPreferredSize(new Dimension(600, 400));

        historyTable = createTable();
        JScrollPane historyScroll = new JScrollPane(historyTable);
        historyTable.setFillsViewportHeight(true);

        if (historyCombo.getItemCount() > 0)
        {
            selectHistoryItem(historyCombo);
        }

        bottomPanel.add(historyScroll);
        /* End Butt Panel */

        historyFrame.add(topPanel);
        historyFrame.add(bottomPanel);

        historyFrame.pack();
        historyFrame.setVisible(true); // show window
        historyFrame.setLocationRelativeTo(null); // center screen
    }

    private void selectHistoryItem(JComboBox cb)
    {
        if (historyTable != null)
        {
            String name = (String)cb.getSelectedItem();
            // get last 5 mins of history
            ArrayList<Stock> history = system.getStockHistory(name,
                    new Timestamp(new Date().getTime() - (1200 * 1000)),
                    new Timestamp(new Date().getTime()));

            updateTable(historyTable, convertHistoryToTable(history), historyColumns);
        }
    }

    private Vector<Vector<Object>> convertStockToTable(ArrayList<Stock> stocks)
    {
        Vector<Vector<Object>> results = new Vector<>(stocks.size());

        for (Stock s : stocks) {
            Vector<Object> row = new Vector<>(4);
            row.add(s.getName());
            row.add(s.getPrice());
            row.add(s.getDiff());
            results.add(row);
        }

        return results;
    }

    private Vector<Vector<Object>> convertHistoryToTable(ArrayList<Stock> stocks)
    {
        Vector<Vector<Object>> results = new Vector<>(stocks.size());

        for (Stock s : stocks) {
            Vector<Object> row = new Vector<>(4);
            row.add(s.getTime());
            row.add(s.getPrice());
            row.add(s.getDiff());
            results.add(row);
        }

        return results;
    }

    private Vector<Vector<Object>> convertSharesToTable(ArrayList<Share> shares)
    {
        Vector<Vector<Object>> results = new Vector<>(shares.size());

        int totalShares = 0;
        double totalValue = 0.0;

        for(Share s : shares) {

                Vector<Object> row = new Vector<>(3);
                row.add(s.getStock().getName());
                row.add(s.getAmount());
                row.add(s.getStock().getPrice() * s.getAmount());
                results.add(row);
                totalShares += s.getAmount();
                totalValue += s.getStock().getPrice() * s.getAmount();
        }

        Vector<Object> row = new Vector<>(3);
        row.add("Total shares");
        row.add(totalShares);
        row.add(totalValue);
        results.add(row);

        Vector<Object> row2 = new Vector<>(3);
        row2.add("Total");
        row2.add("");
        row2.add(totalValue + system.getAccount().getBalance());
        results.add(row2);


        return results;
    }

    public class enableTask extends TimerTask {
        @Override
        public void run() {
            buyButton.setEnabled(true);
            sellButton.setEnabled(true);
        }
    }

    public void showError(String error)
    {
        JOptionPane.showMessageDialog(frame, error, "Error", JOptionPane.ERROR_MESSAGE);
    }
}

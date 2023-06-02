package grpassignment;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
import java.util.Stack;
import java.util.Queue;
import java.text.SimpleDateFormat;  
import java.util.Date;  
import javax.swing.JOptionPane;
import java.util.Calendar;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class UserInterface extends javax.swing.JFrame implements Runnable {

    static Stack<Integer> stack = new Stack<Integer>();
    static Stack<Integer> enginetime = new Stack<Integer>();
    static Stack<Integer> electrictime = new Stack<Integer>();
    static Queue<Integer> q = new LinkedList<Integer>();
    static Queue<Double> elDistance = new LinkedList<Double>();
    //static Queue<Double> enDistance = new LinkedList<Double>();
    double electricalDistance = 0.00;
    double engineDistance = 0.00;
    double totalElectric = 0.00;
    double carbon = 0.00;

    int engine = 0;
    int battery = 100;

    SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy");  
    SimpleDateFormat datetimeFormatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");  
    Calendar yesterday = Calendar.getInstance();
    Date date = new Date();  
    Thread t = new Thread(this);
    String yesterdayDate;
    Thread chargeThread = new Thread(new charge());
    
    public UserInterface() {
        initComponents();

    }
    
    public UserInterface(String carplate) {       
        initComponents();
               
        yesterday.add(yesterday.DATE, -1);
        yesterdayDate = dateFormatter.format(yesterday.getTime());
        date_time.setText(dateFormatter.format(date));
        
        start_Button.setEnabled(false);
        end_Button.setEnabled(false);
        charge_Button.setEnabled(false);
        start_Button.setVisible(false);
        end_Button.setVisible(false);
        charge_Button.setVisible(false); 
        
        try
        {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            String url = "jdbc:sqlserver://DESKTOP-HHI8NQO:1433;databaseName=GreenCar;encrypt=true;trustServerCertificate=true;integratedSecurity=true";
            Connection con = DriverManager.getConnection(url);
            String sql = "SELECT * FROM Vehicle WHERE car_id=?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, carplate);
            ResultSet rs = pst.executeQuery();
            if (rs.next())
            {
                car_plateid.setText(carplate);
                owner.setText(rs.getString("owner_name").toUpperCase());
                model_name.setText(rs.getString("model").toUpperCase());
            }
            else
            {
                JOptionPane.showMessageDialog(null, "No such Car could be found");
            }
            
        }
        catch(Exception e)
        {
            System.out.println(e);
        }
        
        System.out.println(carplate);
        setUI();
    }
    
    public void setUI()
    {
        int count = -1;
        try
        {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            String url = "jdbc:sqlserver://DESKTOP-HHI8NQO:1433;databaseName=GreenCar;encrypt=true;trustServerCertificate=true;integratedSecurity=true";
            Connection con = DriverManager.getConnection(url);
            
            String sql = "SELECT Count(*) AS recordCount FROM Green WHERE record_date=? AND car_id =? ";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, date_time.getText());
            pst.setString(2, car_plateid.getText());
            ResultSet rs = pst.executeQuery();
            if (rs.next())
            {
                
                count = rs.getInt(1);
            }
            
            if (count == 0)
            {
                sql = "SELECT * FROM Green WHERE record_date=? AND car_id =? ";
                pst = con.prepareStatement(sql);
                pst.setString(1, yesterdayDate);
                pst.setString(2, car_plateid.getText());
                rs = pst.executeQuery();
                if (rs.next())
                {
                    battery = rs.getInt(6);
                }
            }
            else
            {
                sql = "SELECT * FROM Green WHERE record_date=? AND car_id =? ";
                pst = con.prepareStatement(sql);
                pst.setString(1, date_time.getText());
                pst.setString(2, car_plateid.getText());
                rs = pst.executeQuery();
                if (rs.next())
                {
                    totalElectric = rs.getDouble(3);
                    electricalDistance = rs.getDouble(3);
                    engineDistance = rs.getDouble(4);
                    carbon = rs.getDouble(5);
                    battery = rs.getInt(6);
                }
            }       
        }
        catch(Exception e)
        {
            JOptionPane.showMessageDialog(null, "Could Not Display Correct Info");
        }
        
        carbon = (totalElectric / engineDistance) * 100;
        
        if (carbon >= 75)
        {
            statusPanel.setBackground(new java.awt.Color(0, 204, 51));
            statusLabel.setText("  EXCELLENT STATUS");
            statusLabel.setForeground(new java.awt.Color(255, 255, 255));
        }
        else if (carbon <= 30)
        {
            statusPanel.setBackground(new java.awt.Color(255, 0, 0));
            statusLabel.setText("     POOR READING");
            statusLabel.setForeground(new java.awt.Color(255, 255, 255));
        }
        else
        {
            statusPanel.setBackground(new java.awt.Color(255, 153, 0));
            statusLabel.setText("     GOOD STATUS");
            statusLabel.setForeground(new java.awt.Color(255, 255, 255));
        }
        
        for(int i = 0; i <= battery; i++)
           {
               stack.push(i);
               energybar.setValue(stack.peek());
               batteryField.setText(String.valueOf(stack.peek()));
           }  
    }
    
    public void updateStatus(double enDist)
    {
        int count = -1;
        while (!elDistance.isEmpty())
        {
            totalElectric += elDistance.remove();
        }
        
        carbon = (totalElectric / enDist) * 100;
        BigDecimal bd = new BigDecimal(carbon).setScale(2, RoundingMode.HALF_UP);
        carbon = bd.doubleValue();
        
        if (carbon >= 75)
        {
            statusPanel.setBackground(new java.awt.Color(0, 204, 51));
            statusLabel.setText("  EXCELLENT STATUS");
            statusLabel.setForeground(new java.awt.Color(255, 255, 255));
        }
        else if (carbon <= 30)
        {
            statusPanel.setBackground(new java.awt.Color(255, 0, 0));
            statusLabel.setText("     POOR READING");
            statusLabel.setForeground(new java.awt.Color(255, 255, 255));
        }
        else
        {
            statusPanel.setBackground(new java.awt.Color(255, 153, 0));
            statusLabel.setText("     GOOD STATUS");
            statusLabel.setForeground(new java.awt.Color(255, 255, 255));
        }
        
        try
        {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            String url = "jdbc:sqlserver://DESKTOP-HHI8NQO:1433;databaseName=GreenCar;encrypt=true;trustServerCertificate=true;integratedSecurity=true";
            Connection con = DriverManager.getConnection(url);
            
            String sql = "SELECT Count(*) AS recordCount FROM Green WHERE record_date=? AND car_id =? ";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, date_time.getText());
            pst.setString(2, car_plateid.getText());
            ResultSet rs = pst.executeQuery();
            if (rs.next())
            {
                
                count = rs.getInt(1);
            }
            
            if (count == 0)
            {
                sql = "INSERT INTO Green (record_date, car_id, electrical_distance, total_distance, co2_reduction, endBattery) VALUES(?, ?, ?, ?, ?, ?)";
                pst = con.prepareStatement(sql);
                pst.setString(1, date_time.getText());
                pst.setString(2, car_plateid.getText());
                pst.setDouble(3, totalElectric);
                pst.setDouble(4, enDist);
                pst.setDouble(5, carbon);
                pst.setInt(6, stack.peek());
                pst.executeUpdate();
            }
            else
            {
                sql = "UPDATE Green SET electrical_distance=?, total_distance=?, co2_reduction=?, endBattery=? WHERE record_date=? AND car_id =? ";
                pst = con.prepareStatement(sql);
                pst.setDouble(1, totalElectric);
                pst.setDouble(2, enDist);
                pst.setDouble(3, carbon);
                pst.setInt(4, stack.peek());
                pst.setString(5, date_time.getText());
                pst.setString(6, car_plateid.getText());
                pst.executeUpdate();
            }
        }
        catch(Exception e)
        {
              JOptionPane.showMessageDialog(null, "Record Could not be Added");

        }
    }
    
    private class charge implements Runnable {
 
        public void run()
        {
            /*while (q.isEmpty())
            {
                battery = q.remove(); 
            }*/
            for(int i = battery; i <= 100; i++)
                {
                    try{
                        Thread.sleep(1000);
                        stack.push(i);
                        energybar.setValue(stack.peek());
                        batteryField.setText(String.valueOf(stack.peek()));   
                    }
                    catch(Exception e)
                    {
                        System.out.println(e);
                    }                      
                }
        }
    }
    
    @Override
    public void run() {
        
        try{
            if(q.isEmpty())
            {
                 for(int i = stack.peek(); i >= 0; i--)
                    {
                        Thread.sleep(1000);
                        stack.pop();
                        energybar.setValue(stack.peek());
                        batteryField.setText(String.valueOf(stack.peek()));
                    }  
            }
            else
            {
                for(int i = q.remove(); i >= 0; i--)
                    {
                        Thread.sleep(1000);
                        stack.pop();
                        energybar.setValue(stack.peek());
                        batteryField.setText(String.valueOf(stack.peek()));
                    }  
            }
            
        }catch(Exception e) {
            System.out.println(e);
        }
        
    }


    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        car_plateid = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        date_time = new javax.swing.JLabel();
        owner = new javax.swing.JLabel();
        model_name = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jlabel = new javax.swing.JLabel();
        jlabel1 = new javax.swing.JLabel();
        statusPanel = new javax.swing.JPanel();
        statusLabel = new javax.swing.JLabel();
        end_Button = new javax.swing.JButton();
        start_Button = new javax.swing.JButton();
        energybar = new javax.swing.JProgressBar();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        batteryField = new javax.swing.JTextField();
        charge_Button = new javax.swing.JButton();
        startstopButton = new javax.swing.JButton();

        jLabel1.setText("jLabel1");

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Green Car Application");
        setBackground(new java.awt.Color(255, 255, 255));
        setLocation(new java.awt.Point(395, 0));
        setName("user_inteface"); // NOI18N
        setSize(new java.awt.Dimension(540, 960));

        car_plateid.setFont(new java.awt.Font("Arial Rounded MT Bold", 1, 60)); // NOI18N
        car_plateid.setText("-");

        jLabel2.setFont(new java.awt.Font("Arial Rounded MT Bold", 1, 14)); // NOI18N
        jLabel2.setText("PLATE NUMBER");

        date_time.setFont(new java.awt.Font("Arial Rounded MT Bold", 1, 14)); // NOI18N

        owner.setFont(new java.awt.Font("Arial Rounded MT Bold", 1, 18)); // NOI18N
        owner.setText("XXXXXX");

        model_name.setFont(new java.awt.Font("Arial Rounded MT Bold", 1, 18)); // NOI18N
        model_name.setText(".");

        jlabel.setFont(new java.awt.Font("Arial Rounded MT Bold", 1, 18)); // NOI18N
        jlabel.setText("CO2 EMISSION ");

        jlabel1.setFont(new java.awt.Font("Arial Rounded MT Bold", 1, 18)); // NOI18N
        jlabel1.setText("REDUCED");

        statusPanel.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        statusPanel.setToolTipText("");

        statusLabel.setFont(new java.awt.Font("Arial Rounded MT Bold", 1, 22)); // NOI18N
        statusLabel.setForeground(new java.awt.Color(0, 0, 204));
        statusLabel.setText("       INAPPLICABLE");

        javax.swing.GroupLayout statusPanelLayout = new javax.swing.GroupLayout(statusPanel);
        statusPanel.setLayout(statusPanelLayout);
        statusPanelLayout.setHorizontalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(statusLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 266, Short.MAX_VALUE)
                .addContainerGap())
        );
        statusPanelLayout.setVerticalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(statusLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 61, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(45, 45, 45)
                        .addComponent(jlabel1))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(27, 27, 27)
                        .addComponent(jlabel)))
                .addGap(18, 18, 18)
                .addComponent(statusPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(31, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(49, 49, 49)
                        .addComponent(jlabel, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jlabel1))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(35, 35, 35)
                        .addComponent(statusPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(46, Short.MAX_VALUE))
        );

        end_Button.setBackground(new java.awt.Color(255, 0, 0));
        end_Button.setFont(new java.awt.Font("Arial Rounded MT Bold", 1, 24)); // NOI18N
        end_Button.setForeground(new java.awt.Color(255, 255, 255));
        end_Button.setText("STOP");
        end_Button.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        end_Button.setMaximumSize(new java.awt.Dimension(69, 25));
        end_Button.setMinimumSize(new java.awt.Dimension(69, 25));
        end_Button.setPreferredSize(new java.awt.Dimension(69, 25));
        end_Button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                end_ButtonActionPerformed(evt);
            }
        });

        start_Button.setBackground(new java.awt.Color(0, 153, 0));
        start_Button.setFont(new java.awt.Font("Arial Rounded MT Bold", 1, 18)); // NOI18N
        start_Button.setForeground(new java.awt.Color(255, 255, 255));
        start_Button.setText("START");
        start_Button.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        start_Button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                start_ButtonActionPerformed(evt);
            }
        });

        energybar.setBackground(new java.awt.Color(255, 255, 255));
        energybar.setFont(new java.awt.Font("Arial Rounded MT Bold", 1, 14)); // NOI18N
        energybar.setForeground(new java.awt.Color(0, 0, 0));
        energybar.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 3));
        energybar.setString("");

        jLabel3.setFont(new java.awt.Font("Arial Rounded MT Bold", 1, 14)); // NOI18N
        jLabel3.setText("BATTERY PERCENTAGE :");

        jLabel4.setFont(new java.awt.Font("Arial Rounded MT Bold", 1, 14)); // NOI18N

        batteryField.setEditable(false);
        batteryField.setFont(new java.awt.Font("Arial Rounded MT Bold", 1, 14)); // NOI18N
        batteryField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                batteryFieldActionPerformed(evt);
            }
        });

        charge_Button.setBackground(new java.awt.Color(255, 153, 0));
        charge_Button.setFont(new java.awt.Font("Arial Rounded MT Bold", 1, 18)); // NOI18N
        charge_Button.setForeground(new java.awt.Color(255, 255, 255));
        charge_Button.setText("CHARGE");
        charge_Button.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        charge_Button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                charge_ButtonActionPerformed(evt);
            }
        });

        startstopButton.setBackground(new java.awt.Color(204, 204, 204));
        startstopButton.setFont(new java.awt.Font("Arial Rounded MT Bold", 1, 18)); // NOI18N
        startstopButton.setForeground(new java.awt.Color(0, 0, 204));
        startstopButton.setText("START / STOP ENGINE");
        startstopButton.setBorder(null);
        startstopButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startstopButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(startstopButton, javax.swing.GroupLayout.PREFERRED_SIZE, 265, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(date_time, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(24, 24, 24)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(owner, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 444, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(model_name, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 334, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(car_plateid, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 251, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(83, 83, 83)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addGroup(layout.createSequentialGroup()
                                            .addComponent(start_Button, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(end_Button, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addComponent(energybar, javax.swing.GroupLayout.PREFERRED_SIZE, 352, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jLabel3)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jLabel4)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(batteryField, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(charge_Button, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(9, 9, 9)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(startstopButton, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(2, 2, 2)
                        .addComponent(date_time, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(owner, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(car_plateid, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(model_name, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(end_Button, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(start_Button, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(energybar, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jLabel4)
                    .addComponent(batteryField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(charge_Button, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(290, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    
    private void start_ButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_start_ButtonActionPerformed
        // TODO add your handling code here:
        if (q.isEmpty())
        {
            t.start();
        }
        else
        {
            t.resume();
        }
        
        int hours = 0;
        int mins = 0; 
        int seconds = 0;
        int total = 0;
        java.util.Date date = new java.util.Date();  
        hours = date.getHours()*60*60;
        mins = date.getMinutes()*60;
        seconds = date.getSeconds();
        total = hours + mins + seconds;
        electrictime.push(total);
    }//GEN-LAST:event_start_ButtonActionPerformed

    private void end_ButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_end_ButtonActionPerformed
        // TODO add your handling code here:
         battery = stack.peek();
         t.suspend();
         if(!stack.isEmpty()) 
         {
             q.add(battery);
             energybar.setValue(battery);
         }
         
        int hours = 0;
        int mins = 0; 
        int seconds = 0;
        int total = 0;
        java.util.Date date = new java.util.Date();  
        hours = date.getHours()*60*60;
        mins = date.getMinutes()*60;
        seconds = date.getSeconds();
        total = hours + mins + seconds;
        electrictime.push(total);
        if (electrictime.size() == 2)
        {
            int endTime = electrictime.pop();
            int startTime = electrictime.pop();
            int electricTime = endTime - startTime;
            electricalDistance += 2.25 * electricTime;
            System.out.println("Electric Distance : " + electricalDistance);
            elDistance.add(electricalDistance);
        }
    }//GEN-LAST:event_end_ButtonActionPerformed

    private void batteryFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_batteryFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_batteryFieldActionPerformed

    private void charge_ButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_charge_ButtonActionPerformed
        // TODO 9add your handling code here:
        try{
            if (chargeThread.isAlive())
            {
                chargeThread.stop();
            }
            else
            {
                chargeThread.start();
            }
            if(stack.peek() == 100) 
            {
                chargeThread.stop();
            }
        }
        catch(Exception e)
        {
            System.out.println(e);
        }
    }//GEN-LAST:event_charge_ButtonActionPerformed

    private void startstopButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startstopButtonActionPerformed
        // TODO add your handling code here:
        int hours = 0;
        int mins = 0; 
        int seconds = 0;
        int total = 0;
        java.util.Date date = new java.util.Date();  
        hours = date.getHours()*60*60;
        mins = date.getMinutes()*60;
        seconds = date.getSeconds();
        total = hours + mins + seconds;
        enginetime.push(total);
        if(engine == 0)
        {
            engine = 1;
            start_Button.setEnabled(true);
            end_Button.setEnabled(true);
            charge_Button.setEnabled(true);
            start_Button.setVisible(true);
            end_Button.setVisible(true);
            charge_Button.setVisible(true);
        }
        else
        {
            engine = 0;
            start_Button.setEnabled(false);
            end_Button.setEnabled(false);
            charge_Button.setEnabled(false);
            start_Button.setVisible(false);
            end_Button.setVisible(false);
            charge_Button.setVisible(false);
            
            battery = stack.peek();
            t.suspend();
            if(!stack.isEmpty()) 
            {
                q.add(battery);
                energybar.setValue(battery);
            }
            if (chargeThread.isAlive())
            {
                chargeThread.stop();
            }
            
            if(electrictime.size() == 1)
            {
                date = new java.util.Date();  
                hours = date.getHours()*60*60;
                mins = date.getMinutes()*60;
                seconds = date.getSeconds();
                total = hours + mins + seconds;
                electrictime.push(total);
                if (electrictime.size() == 2)
                {
                    int endTime = electrictime.pop();
                    int startTime = electrictime.pop();
                    int electricTime = endTime - startTime;
                    electricalDistance += 2.25 * electricTime;
                    System.out.println("Electric Distance : " + electricalDistance);
                    elDistance.add(electricalDistance);
                }
            }
        }
        
        if (enginetime.size() == 2)
        {
            int endTime = enginetime.pop();
            int startTime = enginetime.pop();
            int engineTime = endTime - startTime;
            engineDistance += 2.25 * engineTime;
            updateStatus(engineDistance);
            System.out.println("Engine Distance : " + engineDistance);
        }
        
    }//GEN-LAST:event_startstopButtonActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(UserInterface.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(UserInterface.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(UserInterface.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(UserInterface.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        
        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new UserInterface().setVisible(true);
                
            }
        });
        
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField batteryField;
    public javax.swing.JLabel car_plateid;
    private javax.swing.JButton charge_Button;
    private javax.swing.JLabel date_time;
    private javax.swing.JButton end_Button;
    public static javax.swing.JProgressBar energybar;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel jlabel;
    private javax.swing.JLabel jlabel1;
    public javax.swing.JLabel model_name;
    public javax.swing.JLabel owner;
    private javax.swing.JButton start_Button;
    private javax.swing.JButton startstopButton;
    private javax.swing.JLabel statusLabel;
    private javax.swing.JPanel statusPanel;
    // End of variables declaration//GEN-END:variables

   
}

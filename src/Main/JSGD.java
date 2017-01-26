/*
 * JSGD.java
 *
 * Created on May 11, 2005, 10:51 AM
 */

package Main;

import SecuGen.FDxSDKPro.jni.*;

import java.awt.*;
import java.awt.image.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.swing.*;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;


/**
 *
 * @author  Castillo, Guiña, Jova
 */
public class JSGD extends javax.swing.JFrame {
	
	
	
	//DATOS PARA LA CONEXION
	   private String bd = "lector";
	   private String user = "postgres";
	   private String password = "Fernando30";
	   private String url = "jdbc:postgresql://localhost:5432/"+bd;
	   private int hola;

	   private Connection connection = null;
	   private ResultSet resultSet = null;
	   private Statement statement = null;
	   
    
    //Private instance variables
    private long deviceName;
    
    private static byte[] registro1;
    private static byte[] registro2;
    
    private long devicePort;
    private JSGFPLib fplib = null;
    private long ret;
    private boolean bLEDOn;
    private byte[] regMin1 = new byte[400];
    private byte[] regMin2 = new byte[400];
    private byte[] vrfMin  = new byte[400];
    private SGDeviceInfoParam deviceInfo = new SGDeviceInfoParam();
    private BufferedImage imgRegistration1;
    private BufferedImage imgRegistration2;
    private BufferedImage imgVerification;
    private boolean r1Captured = false;
    private boolean r2Captured = false;
    private boolean v1Captured = false;
    private static int MINIMUM_QUALITY = 40;       //User defined
    private static int MINIMUM_NUM_MINUTIAE = 15;  //User defined
    private static int MAXIMUM_NFIQ = 2;           //User defined
    String name="";
    
    
    /** Creates new form JSGD */
    public JSGD() {
        bLEDOn = false;
        initComponents();
        disableControls();
        this.jComboBoxRegisterSecurityLevel.setSelectedIndex(4);
        this.jComboBoxVerifySecurityLevel.setSelectedIndex(4);
        
        JButton btnCedula = new JButton();
        btnCedula.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent arg0) {
        		name = String.valueOf(JOptionPane.showInputDialog(null, "Ingrese su cédula"));

        	      
                System.out.printf("el id es " +  name +  "\n");
        		
        		
        	}
        });
        btnCedula.setText("Buscar C\u00E9dula");
        btnCedula.setPreferredSize(new Dimension(270, 30));
        btnCedula.setMinimumSize(new Dimension(270, 30));
        btnCedula.setMaximumSize(new Dimension(270, 30));
        btnCedula.setActionCommand("jButton1");
        btnCedula.setBounds(12, 321, 284, 30);
        jPanelRegisterVerify.add(btnCedula);
        
        JLabel lblEvento = new JLabel();
        lblEvento.setText("Evento:");
        lblEvento.setBounds(377, 28, 44, 14);
        jPanelRegisterVerify.add(lblEvento);
        
        JComboBox comboBox = new JComboBox();
       // comboBox.setSelectedIndex(4);
        comboBox.setBounds(330, 45, 140, 20);
        jPanelRegisterVerify.add(comboBox);
        
    }
    
    public void conexion(){
    	
    	try{
            Class.forName("org.postgresql.Driver");         
            connection = DriverManager.getConnection(url, user , password);
            System.out.println("Conectado a la base de datos [ " + this.bd + "]");
         }catch(Exception e){
            System.err.println(e.getMessage());
         }
    }
    
    public void guardarHuella(String id, byte[] registro1, byte[] registro2){
    	try {
           
            PreparedStatement pstm = connection.prepareStatement("INSERT into " + " registro(id, registro_uno, registro_dos) " + " VALUES(?,?,?)");
            pstm.setString(1, id);
            pstm.setBytes(2, registro1);
            pstm.setBytes(3, registro2);
            pstm.execute();
            pstm.close();
           
       
       } catch (SQLException e) {
           System.out.println(e.getMessage());
       } 
    }
    
    public void leerTodasHuellas(){
    	byte[] binario1;
    	byte[] binario2;
    	try {
            statement = connection.createStatement();
            resultSet = statement.executeQuery("SELECT id,registro_uno, registro_dos FROM registro " );
          while (resultSet.next())
          {  
             
             binario1= resultSet.getBytes("registro_uno");
             binario2= resultSet.getBytes("registro_dos");
             System.out.println("arreglo 1 es: " + binario1);
             System.out.println("arreglo 2 es: " + binario2);
            
          }
       }
       catch (SQLException ex) {
          System.err.println(ex.getMessage());
       }
    	
    }
    
    public boolean comprobarRegistro(String id) throws SQLException{
    	boolean existe= false;
    	
   
    	
    	String selectSQL = "select registro_uno FROM registro where id = ?";
    	PreparedStatement preparedStatement = connection.prepareStatement(selectSQL);
    	preparedStatement.setString(1, id);
    	ResultSet rs = preparedStatement.executeQuery();
    	while (rs.next()) {
    		existe=true;
    	}
    	
    	return existe;
    	
    }
    
    
    public byte [] obtenerTemplate1(String id) throws SQLException{
    	byte[] binario1= "No existe".getBytes();
    	
    	String selectSQL = "select registro_uno FROM registro where id = ?";
    	PreparedStatement preparedStatement = connection.prepareStatement(selectSQL);
    	preparedStatement.setString(1, id);
    	ResultSet rs = preparedStatement.executeQuery();
    	while (rs.next()) {
    		binario1= rs.getBytes("registro_uno"); 
    	}
              
        
    	
    	return binario1;
    	
    }
    
    public byte [] obtenerTemplate2(String id) throws SQLException{
    	byte[] binario2= "No existe".getBytes();
    	
    	String selectSQL = "select registro_dos FROM registro where id = ?";
    	PreparedStatement preparedStatement = connection.prepareStatement(selectSQL);
    	preparedStatement.setString(1, id);
    	ResultSet rs = preparedStatement.executeQuery();
    	while (rs.next()) {
    		binario2= rs.getBytes("registro_dos"); 
    	}
    	
    	return binario2;
    	
    }
    
    private void disableControls()
    {
        this.jButtonToggleLED.setEnabled(false);
        this.jButtonCapture.setEnabled(false);
        this.jButtonCaptureR1.setEnabled(false);
        this.jButtonCaptureR2.setEnabled(false);
        this.jButtonCaptureV1.setEnabled(false);
        this.jButtonRegister.setEnabled(false);
        this.jButtonGetDeviceInfo.setEnabled(false);
        this.jButtonConfig.setEnabled(false);
    }
    
    private void enableControls()
    {
        this.jButtonToggleLED.setEnabled(true);
        this.jButtonCapture.setEnabled(true);
        this.jButtonCaptureR1.setEnabled(true);
        this.jButtonCaptureR2.setEnabled(true);
        this.jButtonCaptureV1.setEnabled(true);
        this.jButtonGetDeviceInfo.setEnabled(true);
        this.jButtonConfig.setEnabled(true);
     }
    private void enableRegisterAndVerifyControls()
    {
        if (r1Captured && r2Captured)
            this.jButtonRegister.setEnabled(true);
        if (r1Captured && r2Captured && v1Captured)
            this.jButtonVerify.setEnabled(true);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents papuuuuuu
    private void initComponents() {

        jLabelStatus = new javax.swing.JLabel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanelImage = new javax.swing.JPanel();
        jButtonInit = new javax.swing.JButton();
        jButtonInit.setBounds(10, 10, 100, 30);
        jLabelImage = new javax.swing.JLabel();
        jLabelImage.setBounds(10, 60, 260, 300);
        jComboBoxUSBPort = new javax.swing.JComboBox();
        jComboBoxUSBPort.setBounds(280, 90, 170, 27);
        jButtonToggleLED = new javax.swing.JButton();
        jButtonToggleLED.setBounds(131, 10, 123, 30);
        jButtonCapture = new javax.swing.JButton();
        jButtonCapture.setBounds(270, 10, 110, 30);
        jButtonConfig = new javax.swing.JButton();
        jButtonConfig.setBounds(390, 10, 100, 30);
        jLabel1 = new javax.swing.JLabel();
        jLabel1.setBounds(280, 70, 100, 14);
        jSliderQuality = new javax.swing.JSlider();
        jSliderQuality.setBounds(270, 170, 220, 45);
        jLabel2 = new javax.swing.JLabel();
        jLabel2.setBounds(280, 150, 100, 14);
        jLabel3 = new javax.swing.JLabel();
        jLabel3.setBounds(290, 230, 110, 14);
        jSliderSeconds = new javax.swing.JSlider();
        jSliderSeconds.setBounds(270, 250, 220, 45);
        jPanelRegisterVerify = new javax.swing.JPanel();
        jLabelSecurityLevel = new javax.swing.JLabel();
        jLabelSecurityLevel.setBounds(0, 3, 495, 73);
        jLabelRegistration = new javax.swing.JLabel();
        jLabelRegistration.setBounds(52, 28, 63, 14);
        jLabelVerification = new javax.swing.JLabel();
        jLabelVerification.setBounds(206, 28, 78, 14);
        jComboBoxRegisterSecurityLevel = new javax.swing.JComboBox();
        jComboBoxRegisterSecurityLevel.setBounds(22, 45, 128, 20);
        jComboBoxVerifySecurityLevel = new javax.swing.JComboBox();
        jComboBoxVerifySecurityLevel.setBounds(160, 45, 160, 20);
        jLabelRegistrationBox = new javax.swing.JLabel();
        jLabelRegistrationBox.setBounds(10, 80, 290, 240);
        jLabelRegisterImage1 = new javax.swing.JLabel();
        jLabelRegisterImage1.setBounds(20, 100, 130, 150);
        jLabelRegisterImage2 = new javax.swing.JLabel();
        jLabelRegisterImage2.setBounds(160, 100, 130, 150);
        jLabelVerificationBox = new javax.swing.JLabel();
        jLabelVerificationBox.setBounds(320, 80, 150, 240);
        jLabelVerifyImage = new javax.swing.JLabel();
        jLabelVerifyImage.setBounds(330, 100, 130, 150);
        jButtonCaptureR1 = new javax.swing.JButton();
        jButtonCaptureR1.setBounds(20, 280, 130, 30);
        jButtonCaptureV1 = new javax.swing.JButton();
        jButtonCaptureV1.setBounds(330, 280, 130, 30);
        jButtonRegister = new javax.swing.JButton();
        jButtonRegister.setBounds(12, 355, 284, 30);
        jButtonVerify = new javax.swing.JButton();
        jButtonVerify.setBounds(330, 340, 130, 30);
        jButtonCaptureR2 = new javax.swing.JButton();
        jButtonCaptureR2.setBounds(160, 280, 130, 30);
        jProgressBarR1 = new javax.swing.JProgressBar();
        jProgressBarR1.setBounds(20, 250, 130, 14);
        jProgressBarR2 = new javax.swing.JProgressBar();
        jProgressBarR2.setBounds(160, 250, 130, 14);
        jProgressBarV1 = new javax.swing.JProgressBar();
        jProgressBarV1.setBounds(330, 250, 130, 14);
        jPanelDeviceInfo = new javax.swing.JPanel();
        jLabelDeviceInfoGroup = new javax.swing.JLabel();
        jLabelDeviceID = new javax.swing.JLabel();
        jTextFieldDeviceID = new javax.swing.JTextField();
        jLabelFWVersion = new javax.swing.JLabel();
        jTextFieldFWVersion = new javax.swing.JTextField();
        jLabelSerialNumber = new javax.swing.JLabel();
        jTextFieldSerialNumber = new javax.swing.JTextField();
        jLabelImageWidth = new javax.swing.JLabel();
        jTextFieldImageWidth = new javax.swing.JTextField();
        jLabelImageHeight = new javax.swing.JLabel();
        jTextFieldImageHeight = new javax.swing.JTextField();
        jLabelImageDPI = new javax.swing.JLabel();
        jTextFieldImageDPI = new javax.swing.JTextField();
        jLabelBrightness = new javax.swing.JLabel();
        jTextFieldBrightness = new javax.swing.JTextField();
        jLabelContrast = new javax.swing.JLabel();
        jTextFieldContrast = new javax.swing.JTextField();
        jLabelGain = new javax.swing.JLabel();
        jTextFieldGain = new javax.swing.JTextField();
        jButtonGetDeviceInfo = new javax.swing.JButton();
        jComboBoxDeviceName = new javax.swing.JComboBox();
        jLabelDeviceName = new javax.swing.JLabel();
        jLabelSpacer1 = new javax.swing.JLabel();
        jLabelSpacer2 = new javax.swing.JLabel();

        setTitle("Lector de Huellas");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                exitForm(evt);
            }
        });
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabelStatus.setText("Haga Clic en el Boton Inicializar");
        jLabelStatus.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        getContentPane().add(jLabelStatus, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 470, 490, 30));

        jButtonInit.setText("Inicializar");
        jButtonInit.setMaximumSize(new java.awt.Dimension(100, 30));
        jButtonInit.setMinimumSize(new java.awt.Dimension(100, 30));
        jButtonInit.setName("jButtonInit"); // NOI18N
        jButtonInit.setPreferredSize(new java.awt.Dimension(100, 30));
        jButtonInit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonInitActionPerformed(evt);
            }
        });
        jPanelImage.setLayout(null);
        jPanelImage.add(jButtonInit);

        jLabelImage.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        jLabelImage.setMinimumSize(new java.awt.Dimension(260, 300));
        jLabelImage.setPreferredSize(new java.awt.Dimension(260, 300));
        jPanelImage.add(jLabelImage);

        jComboBoxUSBPort.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "AUTO_DETECT", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9" }));
        jComboBoxUSBPort.setMaximumSize(new java.awt.Dimension(170, 27));
        jComboBoxUSBPort.setMinimumSize(new java.awt.Dimension(170, 27));
        jComboBoxUSBPort.setPreferredSize(new java.awt.Dimension(170, 27));
        jPanelImage.add(jComboBoxUSBPort);

        jButtonToggleLED.setText("Prender LED");
        jButtonToggleLED.setMaximumSize(new java.awt.Dimension(100, 30));
        jButtonToggleLED.setMinimumSize(new java.awt.Dimension(100, 30));
        jButtonToggleLED.setPreferredSize(new java.awt.Dimension(100, 30));
        jButtonToggleLED.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonToggleLEDActionPerformed(evt);
            }
        });
        jPanelImage.add(jButtonToggleLED);

        jButtonCapture.setText("Capturar");
        jButtonCapture.setMaximumSize(new java.awt.Dimension(100, 30));
        jButtonCapture.setMinimumSize(new java.awt.Dimension(100, 30));
        jButtonCapture.setPreferredSize(new java.awt.Dimension(100, 30));
        jButtonCapture.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCaptureActionPerformed(evt);
            }
        });
        jPanelImage.add(jButtonCapture);

        jButtonConfig.setText("Configurar");
        jButtonConfig.setMaximumSize(new java.awt.Dimension(100, 30));
        jButtonConfig.setMinimumSize(new java.awt.Dimension(100, 30));
        jButtonConfig.setPreferredSize(new java.awt.Dimension(100, 30));
        jButtonConfig.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonConfigActionPerformed(evt);
            }
        });
        jPanelImage.add(jButtonConfig);

        jLabel1.setText("Dispositivo USB");
        jPanelImage.add(jLabel1);

        jSliderQuality.setMajorTickSpacing(10);
        jSliderQuality.setMinorTickSpacing(5);
        jSliderQuality.setPaintLabels(true);
        jSliderQuality.setPaintTicks(true);
        jSliderQuality.setName(""); // NOI18N
        jSliderQuality.setOpaque(false);
        jPanelImage.add(jSliderQuality);

        jLabel2.setText("Calidad Imagen");
        jPanelImage.add(jLabel2);

        jLabel3.setText("Tiempo (segundos)");
        jPanelImage.add(jLabel3);

        jSliderSeconds.setMajorTickSpacing(1);
        jSliderSeconds.setMaximum(10);
        jSliderSeconds.setMinimum(1);
        jSliderSeconds.setPaintLabels(true);
        jSliderSeconds.setPaintTicks(true);
        jSliderSeconds.setValue(5);
        jPanelImage.add(jSliderSeconds);

        jTabbedPane1.addTab("Imagen", jPanelImage);
        jPanelRegisterVerify.setLayout(null);

        jLabelSecurityLevel.setBorder(javax.swing.BorderFactory.createTitledBorder("Nivel Seguridad"));
        jPanelRegisterVerify.add(jLabelSecurityLevel);

        jLabelRegistration.setText("Registrar");
        jPanelRegisterVerify.add(jLabelRegistration);

        jLabelVerification.setText("Verificacion");
        jPanelRegisterVerify.add(jLabelVerification);

        jComboBoxRegisterSecurityLevel.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Escaza", "Minimo", "Bajo", "Medio", "Medio Normal", "Normal", "Alto", "Muy Alto", "Extrema" }));
        jPanelRegisterVerify.add(jComboBoxRegisterSecurityLevel);

        jComboBoxVerifySecurityLevel.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Escaza", "Minimo", "Bajo", "Medio", "Medio Normal", "Normal", "Alto", "Muy Alto", "Extrema" }));
        jPanelRegisterVerify.add(jComboBoxVerifySecurityLevel);

        jLabelRegistrationBox.setBorder(javax.swing.BorderFactory.createTitledBorder("Registrar"));
        jPanelRegisterVerify.add(jLabelRegistrationBox);

        jLabelRegisterImage1.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        jLabelRegisterImage1.setMinimumSize(new java.awt.Dimension(130, 150));
        jLabelRegisterImage1.setPreferredSize(new java.awt.Dimension(130, 150));
        jPanelRegisterVerify.add(jLabelRegisterImage1);

        jLabelRegisterImage2.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        jLabelRegisterImage2.setMinimumSize(new java.awt.Dimension(130, 150));
        jLabelRegisterImage2.setPreferredSize(new java.awt.Dimension(130, 150));
        jPanelRegisterVerify.add(jLabelRegisterImage2);

        jLabelVerificationBox.setBorder(javax.swing.BorderFactory.createTitledBorder("Verificacion"));
        jPanelRegisterVerify.add(jLabelVerificationBox);

        jLabelVerifyImage.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        jLabelVerifyImage.setMinimumSize(new java.awt.Dimension(130, 150));
        jLabelVerifyImage.setPreferredSize(new java.awt.Dimension(130, 150));
        jPanelRegisterVerify.add(jLabelVerifyImage);

        jButtonCaptureR1.setText("Capturar R1");
        jButtonCaptureR1.setActionCommand("jButton1");
        jButtonCaptureR1.setMaximumSize(new java.awt.Dimension(130, 30));
        jButtonCaptureR1.setMinimumSize(new java.awt.Dimension(130, 30));
        jButtonCaptureR1.setPreferredSize(new java.awt.Dimension(130, 30));
        jButtonCaptureR1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCaptureR1ActionPerformed(evt);
            }
        });
        jPanelRegisterVerify.add(jButtonCaptureR1);

        jButtonCaptureV1.setText("Capturar");
        jButtonCaptureV1.setActionCommand("jButton1");
        jButtonCaptureV1.setMaximumSize(new java.awt.Dimension(130, 30));
        jButtonCaptureV1.setMinimumSize(new java.awt.Dimension(130, 30));
        jButtonCaptureV1.setPreferredSize(new java.awt.Dimension(130, 30));
        jButtonCaptureV1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCaptureV1ActionPerformed(evt);
            }
        });
        jPanelRegisterVerify.add(jButtonCaptureV1);

        jButtonRegister.setText("Registrar");
        jButtonRegister.setActionCommand("jButton1");
        jButtonRegister.setMaximumSize(new java.awt.Dimension(270, 30));
        jButtonRegister.setMinimumSize(new java.awt.Dimension(270, 30));
        jButtonRegister.setPreferredSize(new java.awt.Dimension(270, 30));
        jButtonRegister.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonRegisterActionPerformed(evt);
            }
        });
        jPanelRegisterVerify.add(jButtonRegister);

        jButtonVerify.setText("Verificar");
        jButtonVerify.setActionCommand("jButton1");
        jButtonVerify.setMaximumSize(new java.awt.Dimension(130, 30));
        jButtonVerify.setMinimumSize(new java.awt.Dimension(130, 30));
        jButtonVerify.setPreferredSize(new java.awt.Dimension(130, 30));
        jButtonVerify.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
					jButtonVerifyActionPerformed(evt);
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
        });
        jPanelRegisterVerify.add(jButtonVerify);

        jButtonCaptureR2.setText("Capturar R2");
        jButtonCaptureR2.setActionCommand("jButton1");
        jButtonCaptureR2.setMaximumSize(new java.awt.Dimension(130, 30));
        jButtonCaptureR2.setMinimumSize(new java.awt.Dimension(130, 30));
        jButtonCaptureR2.setPreferredSize(new java.awt.Dimension(130, 30));
        jButtonCaptureR2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCaptureR2ActionPerformed(evt);
            }
        });
        jPanelRegisterVerify.add(jButtonCaptureR2);

        jProgressBarR1.setForeground(new java.awt.Color(0, 51, 153));
        jPanelRegisterVerify.add(jProgressBarR1);

        jProgressBarR2.setForeground(new java.awt.Color(0, 51, 153));
        jPanelRegisterVerify.add(jProgressBarR2);

        jProgressBarV1.setForeground(new java.awt.Color(0, 51, 153));
        jPanelRegisterVerify.add(jProgressBarV1);

        jTabbedPane1.addTab("Registrar/Verificar", jPanelRegisterVerify);

        jPanelDeviceInfo.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabelDeviceInfoGroup.setBorder(javax.swing.BorderFactory.createTitledBorder("Informacion del dispositivo"));
        jPanelDeviceInfo.add(jLabelDeviceInfoGroup, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, 290, 290));

        jLabelDeviceID.setText("ID Dispositivo");
        jPanelDeviceInfo.add(jLabelDeviceID, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 30, -1, -1));

        jTextFieldDeviceID.setEditable(false);
        jTextFieldDeviceID.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        jPanelDeviceInfo.add(jTextFieldDeviceID, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 30, 160, -1));

        jLabelFWVersion.setText("F/W Version");
        jPanelDeviceInfo.add(jLabelFWVersion, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 60, -1, -1));

        jTextFieldFWVersion.setEditable(false);
        jTextFieldFWVersion.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        jPanelDeviceInfo.add(jTextFieldFWVersion, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 60, 160, -1));

        jLabelSerialNumber.setText("Serial #");
        jPanelDeviceInfo.add(jLabelSerialNumber, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 90, -1, -1));

        jTextFieldSerialNumber.setEditable(false);
        jTextFieldSerialNumber.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        jPanelDeviceInfo.add(jTextFieldSerialNumber, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 90, 160, -1));

        jLabelImageWidth.setText("Ancho Imagen");
        jPanelDeviceInfo.add(jLabelImageWidth, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 120, -1, -1));

        jTextFieldImageWidth.setEditable(false);
        jTextFieldImageWidth.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        jPanelDeviceInfo.add(jTextFieldImageWidth, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 120, 160, -1));

        jLabelImageHeight.setText("Alto Imagen");
        jPanelDeviceInfo.add(jLabelImageHeight, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 150, -1, -1));

        jTextFieldImageHeight.setEditable(false);
        jTextFieldImageHeight.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        jPanelDeviceInfo.add(jTextFieldImageHeight, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 150, 160, -1));

        jLabelImageDPI.setText("Imagen DPI");
        jPanelDeviceInfo.add(jLabelImageDPI, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 180, -1, -1));

        jTextFieldImageDPI.setEditable(false);
        jTextFieldImageDPI.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        jPanelDeviceInfo.add(jTextFieldImageDPI, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 180, 160, -1));

        jLabelBrightness.setText("Brillo");
        jPanelDeviceInfo.add(jLabelBrightness, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 210, -1, -1));

        jTextFieldBrightness.setEditable(false);
        jTextFieldBrightness.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        jPanelDeviceInfo.add(jTextFieldBrightness, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 210, 160, -1));

        jLabelContrast.setText("Contraste");
        jPanelDeviceInfo.add(jLabelContrast, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 240, -1, -1));

        jTextFieldContrast.setEditable(false);
        jTextFieldContrast.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        jPanelDeviceInfo.add(jTextFieldContrast, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 240, 160, -1));

        jLabelGain.setText("Ganancia");
        jPanelDeviceInfo.add(jLabelGain, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 270, -1, -1));

        jTextFieldGain.setEditable(false);
        jTextFieldGain.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        jPanelDeviceInfo.add(jTextFieldGain, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 270, 160, -1));

        jButtonGetDeviceInfo.setText("Info. Dispositivo");
        jButtonGetDeviceInfo.setMaximumSize(new java.awt.Dimension(150, 30));
        jButtonGetDeviceInfo.setMinimumSize(new java.awt.Dimension(150, 30));
        jButtonGetDeviceInfo.setPreferredSize(new java.awt.Dimension(150, 30));
        jButtonGetDeviceInfo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonGetDeviceInfoActionPerformed(evt); 
            }
        });
        jPanelDeviceInfo.add(jButtonGetDeviceInfo, new org.netbeans.lib.awtextra.AbsoluteConstraints(320, 20, 150, 30));

        jTabbedPane1.addTab("Informacion Dispositivo", jPanelDeviceInfo);

        getContentPane().add(jTabbedPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 35, 500, 420));

        jComboBoxDeviceName.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "AUTO", "FDU06", "FDU05",  "FDU04", "FDU03", "FDU02" }));
        jComboBoxDeviceName.setMinimumSize(new java.awt.Dimension(350, 10));
        jComboBoxDeviceName.setVerifyInputWhenFocusTarget(false);
        getContentPane().add(jComboBoxDeviceName, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 10, 350, -1));

        jLabelDeviceName.setText("Nombre Dispositivo");	
        getContentPane().add(jLabelDeviceName, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 11, 110, -1));

        jLabelSpacer1.setText(" ");
        getContentPane().add(jLabelSpacer1, new org.netbeans.lib.awtextra.AbsoluteConstraints(510, 490, 10, -1));

        jLabelSpacer2.setText(" ");
        getContentPane().add(jLabelSpacer2, new org.netbeans.lib.awtextra.AbsoluteConstraints(510, 10, 10, -1));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonGetDeviceInfoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonGetDeviceInfoActionPerformed
        long iError;

        iError = fplib.GetDeviceInfo(deviceInfo);
        if (ret == SGFDxErrorCode.SGFDX_ERROR_NONE)
        {
            this.jLabelStatus.setText( "GetDeviceInfo() Success");
            this.jTextFieldSerialNumber.setText(new String(deviceInfo.deviceSN()));
            this.jTextFieldBrightness.setText(new String(Integer.toString(deviceInfo.brightness)));
            this.jTextFieldContrast.setText(new String(Integer.toString((int)deviceInfo.contrast)));
            this.jTextFieldDeviceID.setText(new String(Integer.toString(deviceInfo.deviceID)));
            this.jTextFieldFWVersion.setText(new String(Integer.toHexString(deviceInfo.FWVersion)));
            this.jTextFieldGain.setText(new String(Integer.toString(deviceInfo.gain)));
            this.jTextFieldImageDPI.setText(new String(Integer.toString(deviceInfo.imageDPI)));
            this.jTextFieldImageHeight.setText(new String(Integer.toString(deviceInfo.imageHeight)));
            this.jTextFieldImageWidth.setText(new String(Integer.toString(deviceInfo.imageWidth)));
        }
         else
            this.jLabelStatus.setText( "GetDeviceInfo() Error : " + iError);
         
    }//GEN-LAST:event_jButtonGetDeviceInfoActionPerformed

    private void jButtonConfigActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonConfigActionPerformed
        long iError;

        iError = fplib.Configure(0);
        if (iError == SGFDxErrorCode.SGFDX_ERROR_NONE)
        {
           this.jLabelStatus.setText( "Configure() Success");
           this.jButtonGetDeviceInfo.doClick();
        }
        else if (iError == SGFDxErrorCode.SGFDX_ERROR_NOT_USED)
           this.jLabelStatus.setText( "Configure() not supported on this platform");
        else
           this.jLabelStatus.setText( "Configure() Error : " + iError);
        
        
    }//GEN-LAST:event_jButtonConfigActionPerformed

    private void jButtonVerifyActionPerformed(java.awt.event.ActionEvent evt) throws SQLException {//GEN-FIRST:event_jButtonVerifyActionPerformed
         long iError;
         long secuLevel = (long) (this.jComboBoxVerifySecurityLevel.getSelectedIndex() + 1);
         boolean[] matched = new boolean[1];
         matched[0] = false;
         
         Calendar calendario = new GregorianCalendar();
         int hora =calendario.get(Calendar.HOUR_OF_DAY);
         int minutos = calendario.get(Calendar.MINUTE);
         int segundos = calendario.get(Calendar.SECOND);
         System.out.println(hora + ":" + minutos + ":" + segundos);
         //if(minutos )
         String id = String.valueOf(JOptionPane.showInputDialog(null, "Ingrese id para hacer la busqueda"));
         boolean resultado= comprobarRegistro(id);
         if(resultado){
        	 regMin1= obtenerTemplate1(id);
        	 regMin2= obtenerTemplate2(id);
        	 iError = fplib.MatchTemplate(regMin1, vrfMin, secuLevel, matched);
             System.out.println("verificacion registro 1 " + regMin1);
             if (iError == SGFDxErrorCode.SGFDX_ERROR_NONE)
             {
                 if (matched[0])
                    this.jLabelStatus.setText( "Verification Success (1st template)");
                 else
                 {
                     iError = fplib.MatchTemplate(regMin2, vrfMin, secuLevel, matched);
                     System.out.println("verificacion registro 2 " + regMin2);
                     if (iError == SGFDxErrorCode.SGFDX_ERROR_NONE)
                         if (matched[0])
                            this.jLabelStatus.setText( "Verification Success (2nd template)");
                         else
                            this.jLabelStatus.setText( "Verification Fail");
                     else
                        this.jLabelStatus.setText( "Verification Attempt 2 Fail - MatchTemplate() Error : " + iError);
                     
                 }                             
             }
             else
             {
                this.jLabelStatus.setText( "Verification Attempt 1 Fail - MatchTemplate() Error : " + iError);   
             } 
         }
         else{
        	 JOptionPane.showMessageDialog(this, "error no existe el id");
         }
         
          
    }//GEN-LAST:event_jButtonVerifyActionPerformed

    private void jButtonRegisterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonRegisterActionPerformed
         int[] matchScore = new int[1];
         boolean[] matched = new boolean[1];
         long iError;
         long secuLevel = (long) (this.jComboBoxRegisterSecurityLevel.getSelectedIndex() + 1);
         matched[0] = false;
         
         iError = fplib.MatchTemplate(regMin1,regMin2, secuLevel, matched); 
         if (iError == SGFDxErrorCode.SGFDX_ERROR_NONE)
         {
             matchScore[0] = 0;
             iError = fplib.GetMatchingScore(regMin1, regMin2, matchScore);

             if (iError == SGFDxErrorCode.SGFDX_ERROR_NONE)
             {
                 if (matched[0])
                 {
                	 
                	 
                	 guardarHuella(name, regMin1, regMin2);
                     this.jLabelStatus.setText( "Registration Success, Matching Score: " + matchScore[0]);
                 }
                 else
                     this.jLabelStatus.setText( "Registration Fail, Matching Score: " + matchScore[0]);
                     
             }
             else
                this.jLabelStatus.setText( "Registration Fail, GetMatchingScore() Error : " + iError);
         }
             else
                this.jLabelStatus.setText( "Registration Fail, MatchTemplate() Error : " + iError);        
    }//GEN-LAST:event_jButtonRegisterActionPerformed

    private void jButtonCaptureV1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCaptureV1ActionPerformed
        int[] quality = new int[1];
        int[] numOfMinutiae = new int[1];
        byte[] imageBuffer1 = ((java.awt.image.DataBufferByte) imgVerification.getRaster().getDataBuffer()).getData();
        long iError = SGFDxErrorCode.SGFDX_ERROR_NONE;
        
         
        iError = fplib.GetImageEx(imageBuffer1,jSliderSeconds.getValue() * 1000, 0, jSliderQuality.getValue());        
        fplib.GetImageQuality(deviceInfo.imageWidth, deviceInfo.imageHeight, imageBuffer1, quality);
        this.jProgressBarV1.setValue(quality[0]);
        SGFingerInfo fingerInfo = new SGFingerInfo();
        fingerInfo.FingerNumber = SGFingerPosition.SG_FINGPOS_LI;
        fingerInfo.ImageQuality = quality[0];
        fingerInfo.ImpressionType = SGImpressionType.SG_IMPTYPE_LP;
        fingerInfo.ViewNumber = 1;

        if (iError == SGFDxErrorCode.SGFDX_ERROR_NONE)
        {            
            this.jLabelVerifyImage.setIcon(new ImageIcon(imgVerification.getScaledInstance(130,150,Image.SCALE_DEFAULT)));
            if (quality[0] < MINIMUM_QUALITY)
                this.jLabelStatus.setText("GetImageEx() Success [" + ret + "] but image quality is [" + quality[0] + "]. Please try again"); 
            else
            {
                this.jLabelStatus.setText("GetImageEx() Success [" + ret + "]"); 

                iError = fplib.CreateTemplate(fingerInfo, imageBuffer1, vrfMin);
                System.out.println("arreglo verificacion " + vrfMin);
                if (iError == SGFDxErrorCode.SGFDX_ERROR_NONE)
                {
                   long nfiqvalue;        
                   long ret2 = fplib.GetImageQuality(deviceInfo.imageWidth, deviceInfo.imageHeight, imageBuffer1, quality);
                   nfiqvalue = fplib.ComputeNFIQ(imageBuffer1, deviceInfo.imageWidth, deviceInfo.imageHeight);
                   ret2 = fplib.GetNumOfMinutiae(SGFDxTemplateFormat.TEMPLATE_FORMAT_SG400, vrfMin, numOfMinutiae);

                   if ((quality[0] >= MINIMUM_QUALITY) && (nfiqvalue <= MAXIMUM_NFIQ) && (numOfMinutiae[0] >= MINIMUM_NUM_MINUTIAE))
                   {
                      this.jLabelStatus.setText("Verification Capture PASS QC. Quality[" + quality[0] + "] NFIQ[" + nfiqvalue + "] Minutiae[" + numOfMinutiae[0] + "]");
                      v1Captured = true; 
                      this.jButtonVerify.setEnabled(true);
                      this.enableRegisterAndVerifyControls();
                   }
                   else
                   {
                      this.jLabelStatus.setText("Verification Capture FAIL QC. Quality[" + quality[0] + "] NFIQ[" + nfiqvalue + "] Minutiae[" + numOfMinutiae[0] + "]");
                      this.jButtonVerify.setEnabled(false);
                   }
                }
                else
                   this.jLabelStatus.setText("CreateTemplate() Error : " + iError);
            }
         }
         else
            this.jLabelStatus.setText("GetImageEx() Error : " + iError);
        
        
    }//GEN-LAST:event_jButtonCaptureV1ActionPerformed

    private void jButtonCaptureR2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCaptureR2ActionPerformed
        int[] quality = new int[1];
        int[] numOfMinutiae = new int[1];
        byte[] imageBuffer1 = ((java.awt.image.DataBufferByte) imgRegistration2.getRaster().getDataBuffer()).getData();
        
        //registro2= imageBuffer1;
        long iError = SGFDxErrorCode.SGFDX_ERROR_NONE;
         
        iError = fplib.GetImageEx(imageBuffer1,jSliderSeconds.getValue() * 1000, 0, jSliderQuality.getValue());        
        fplib.GetImageQuality(deviceInfo.imageWidth, deviceInfo.imageHeight, imageBuffer1, quality);
        this.jProgressBarR2.setValue(quality[0]);
        SGFingerInfo fingerInfo = new SGFingerInfo();
        fingerInfo.FingerNumber = SGFingerPosition.SG_FINGPOS_LI;
        fingerInfo.ImageQuality = quality[0];
        fingerInfo.ImpressionType = SGImpressionType.SG_IMPTYPE_LP;
        fingerInfo.ViewNumber = 1;

        if (iError == SGFDxErrorCode.SGFDX_ERROR_NONE)
        {            
            this.jLabelRegisterImage2.setIcon(new ImageIcon(imgRegistration2.getScaledInstance(130,150,Image.SCALE_DEFAULT)));
            if (quality[0] < MINIMUM_QUALITY)
                this.jLabelStatus.setText("GetImageEx() Success [" + ret + "] but image quality is [" + quality[0] + "]. Please try again"); 
            else
            {            
                this.jLabelStatus.setText("GetImageEx() Success [" + ret + "]"); 

                iError = fplib.CreateTemplate(fingerInfo, imageBuffer1, regMin2);
                System.out.println("arreglo capture R2" + regMin2);
                if (iError == SGFDxErrorCode.SGFDX_ERROR_NONE)
                {

                   long nfiqvalue;        
                   long ret2 = fplib.GetImageQuality(deviceInfo.imageWidth, deviceInfo.imageHeight, imageBuffer1, quality);
                   nfiqvalue = fplib.ComputeNFIQ(imageBuffer1, deviceInfo.imageWidth, deviceInfo.imageHeight);
                   ret2 = fplib.GetNumOfMinutiae(SGFDxTemplateFormat.TEMPLATE_FORMAT_SG400, regMin2, numOfMinutiae);
                   if ((quality[0] >= MINIMUM_QUALITY) && (nfiqvalue <= MAXIMUM_NFIQ) && (numOfMinutiae[0] >= MINIMUM_NUM_MINUTIAE))
                   { 
                      this.jLabelStatus.setText("Reg. Capture 2 PASS QC. Qual[" + quality[0] + "] NFIQ[" + nfiqvalue + "] Minutiae[" + numOfMinutiae[0] + "]");
                      r2Captured = true;
                      this.enableRegisterAndVerifyControls();
                   }
                   else
                   {
                      this.jLabelStatus.setText("Reg. Capture 2 FAIL QC. Quality[" + quality[0] + "] NFIQ[" + nfiqvalue + "] Minutiae[" + numOfMinutiae[0] + "]");
                      this.jButtonVerify.setEnabled(false);
                      this.jButtonRegister.setEnabled(false);
                   }

                }
                else
                   this.jLabelStatus.setText("CreateTemplate() Error : " + iError);
            }
         }
         else
            this.jLabelStatus.setText("GetImageEx() Error : " + iError);
        
        
    }//GEN-LAST:event_jButtonCaptureR2ActionPerformed

    private void jButtonCaptureR1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCaptureR1ActionPerformed
        int[] quality = new int[1];
        int[] numOfMinutiae = new int[1];
        byte[] imageBuffer1 = ((java.awt.image.DataBufferByte) imgRegistration1.getRaster().getDataBuffer()).getData();
        
        //registro1= imageBuffer1;
        long iError = SGFDxErrorCode.SGFDX_ERROR_NONE;
         
        iError = fplib.GetImageEx(imageBuffer1,jSliderSeconds.getValue() * 1000, 0, jSliderQuality.getValue());        
        fplib.GetImageQuality(deviceInfo.imageWidth, deviceInfo.imageHeight, imageBuffer1, quality);
        this.jProgressBarR1.setValue(quality[0]);
        SGFingerInfo fingerInfo = new SGFingerInfo();
        fingerInfo.FingerNumber = SGFingerPosition.SG_FINGPOS_LI;
        fingerInfo.ImageQuality = quality[0];
        fingerInfo.ImpressionType = SGImpressionType.SG_IMPTYPE_LP;
        fingerInfo.ViewNumber = 1;

        if (iError == SGFDxErrorCode.SGFDX_ERROR_NONE)
        {            
            this.jButtonVerify.setEnabled(false);
            this.jButtonRegister.setEnabled(false);
            this.jLabelRegisterImage1.setIcon(new ImageIcon(imgRegistration1.getScaledInstance(130,150,Image.SCALE_DEFAULT)));
            if (quality[0] < MINIMUM_QUALITY)
                this.jLabelStatus.setText("GetImageEx() Success [" + ret + "] but image quality is [" + quality[0] + "]. Please try again"); 
            else
            {
            
                this.jLabelStatus.setText("GetImageEx() Success [" + ret + "]"); 

                iError = fplib.CreateTemplate(fingerInfo, imageBuffer1, regMin1);
                System.out.println("arreglo capture R1 " + regMin1);
                
                if (iError == SGFDxErrorCode.SGFDX_ERROR_NONE)
                {
                  long nfiqvalue;        
                  long ret2 = fplib.GetImageQuality(deviceInfo.imageWidth, deviceInfo.imageHeight, imageBuffer1, quality);
                  nfiqvalue = fplib.ComputeNFIQ(imageBuffer1, deviceInfo.imageWidth, deviceInfo.imageHeight);
                  ret2 = fplib.GetNumOfMinutiae(SGFDxTemplateFormat.TEMPLATE_FORMAT_SG400, regMin1, numOfMinutiae);

                  if ((quality[0] >= MINIMUM_QUALITY) && (nfiqvalue <= MAXIMUM_NFIQ) && (numOfMinutiae[0] >= MINIMUM_NUM_MINUTIAE))
                  { 
                      this.jLabelStatus.setText("Reg. Capture 1 PASS QC. Qual[" + quality[0] + "] NFIQ[" + nfiqvalue + "] Minutiae[" + numOfMinutiae[0] + "]");
                    r1Captured = true;
                    this.enableRegisterAndVerifyControls();
                  }
                  else
                  {
                      this.jLabelStatus.setText("Reg. Capture 1 FAIL QC. Quality[" + quality[0] + "] NFIQ[" + nfiqvalue + "] Minutiae[" + numOfMinutiae[0] + "]");
                      this.jButtonVerify.setEnabled(false);
                      this.jButtonRegister.setEnabled(false);
                  }
                }
                 else
                   this.jLabelStatus.setText("CreateTemplate() Error : " + iError);
            }
         }
         else
            this.jLabelStatus.setText("GetImageEx() Error : " + iError);
        
        
    }//GEN-LAST:event_jButtonCaptureR1ActionPerformed

    private void jButtonCaptureActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCaptureActionPerformed
        int[] quality = new int[1];     
        long nfiqvalue;           
        BufferedImage img1gray = new BufferedImage(deviceInfo.imageWidth, deviceInfo.imageHeight, BufferedImage.TYPE_BYTE_GRAY);
        byte[] imageBuffer1 = ((java.awt.image.DataBufferByte) img1gray.getRaster().getDataBuffer()).getData();
        if (fplib != null)
        {
            ret = fplib.GetImageEx(imageBuffer1,jSliderSeconds.getValue() * 1000, 0, jSliderQuality.getValue());
            if (ret == SGFDxErrorCode.SGFDX_ERROR_NONE)
            {
                this.jLabelImage.setIcon(new ImageIcon(img1gray));
                long ret2 = fplib.GetImageQuality(deviceInfo.imageWidth, deviceInfo.imageHeight, imageBuffer1, quality);
                nfiqvalue = fplib.ComputeNFIQ(imageBuffer1, deviceInfo.imageWidth, deviceInfo.imageHeight);
                this.jLabelStatus.setText("getImage() Success [" + ret + "] --- Image Quality [" + quality[0] + "] --- NFIQ Value [" + nfiqvalue + "]"); 
            }
            else
            {
                this.jLabelStatus.setText("GetImageEx() Error [" + ret + "]");                                
            }
        } 
        else
        {
            this.jLabelStatus.setText("JSGFPLib is not Initialized");
        }        

    }//GEN-LAST:event_jButtonCaptureActionPerformed

    private void jButtonToggleLEDActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonToggleLEDActionPerformed
        if (fplib != null)
        {
            bLEDOn = !bLEDOn;
            ret = fplib.SetLedOn(bLEDOn);
            if (ret == SGFDxErrorCode.SGFDX_ERROR_NONE)
            {
                this.jLabelStatus.setText("SetLedOn(" + bLEDOn + ") Success [" + ret + "]");                
            }
            else
            {
                this.jLabelStatus.setText("SetLedOn(" + bLEDOn + ") Error [" + ret + "]");                                
            }
        } 
        else
        {
            this.jLabelStatus.setText("JSGFPLib is not Initialized");
        }        
    }//GEN-LAST:event_jButtonToggleLEDActionPerformed

    private void jButtonInitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonInitActionPerformed
        int selectedDevice = jComboBoxDeviceName.getSelectedIndex();
        switch(selectedDevice)
        {
            case 0: //USB
            default:
                this.deviceName = SGFDxDeviceName.SG_DEV_AUTO;
                break;
            case 1: //FDU06
                this.deviceName = SGFDxDeviceName.SG_DEV_FDU06;
                break;
            case 2: //FDU05
                this.deviceName = SGFDxDeviceName.SG_DEV_FDU05;
                break;
            case 3: //FDU04
                this.deviceName = SGFDxDeviceName.SG_DEV_FDU04;
                break;
            case 4: //CN_FDU03
                this.deviceName = SGFDxDeviceName.SG_DEV_FDU03;
                break;
            case 5: //CN_FDU02
                this.deviceName = SGFDxDeviceName.SG_DEV_FDU02;
                break;
        }
        fplib = new JSGFPLib();
        ret = fplib.Init(this.deviceName);
        if ((fplib != null) && (ret  == SGFDxErrorCode.SGFDX_ERROR_NONE))
        {
            this.jLabelStatus.setText("JSGFPLib Initialization Success");
            this.devicePort = SGPPPortAddr.AUTO_DETECT;
            switch (this.jComboBoxUSBPort.getSelectedIndex())
            {
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                case 7:
                case 8:
                case 9:
                case 10:
                    this.devicePort = this.jComboBoxUSBPort.getSelectedIndex() - 1;
                    break;
            }
            ret = fplib.OpenDevice(this.devicePort);
            if (ret == SGFDxErrorCode.SGFDX_ERROR_NONE)
            {
                this.jLabelStatus.setText("OpenDevice() Success [" + ret + "]");       
                ret = fplib.GetDeviceInfo(deviceInfo);
                if (ret == SGFDxErrorCode.SGFDX_ERROR_NONE)
                {
                    this.jTextFieldSerialNumber.setText(new String(deviceInfo.deviceSN()));
                    this.jTextFieldBrightness.setText(new String(Integer.toString(deviceInfo.brightness)));
                    this.jTextFieldContrast.setText(new String(Integer.toString((int)deviceInfo.contrast)));
                    this.jTextFieldDeviceID.setText(new String(Integer.toString(deviceInfo.deviceID)));
                    this.jTextFieldFWVersion.setText(new String(Integer.toHexString(deviceInfo.FWVersion)));
                    this.jTextFieldGain.setText(new String(Integer.toString(deviceInfo.gain)));
                    this.jTextFieldImageDPI.setText(new String(Integer.toString(deviceInfo.imageDPI)));
                    this.jTextFieldImageHeight.setText(new String(Integer.toString(deviceInfo.imageHeight)));
                    this.jTextFieldImageWidth.setText(new String(Integer.toString(deviceInfo.imageWidth)));
                    imgRegistration1 = new BufferedImage(deviceInfo.imageWidth, deviceInfo.imageHeight, BufferedImage.TYPE_BYTE_GRAY);
                    imgRegistration2 = new BufferedImage(deviceInfo.imageWidth, deviceInfo.imageHeight, BufferedImage.TYPE_BYTE_GRAY);
                    imgVerification = new BufferedImage(deviceInfo.imageWidth, deviceInfo.imageHeight, BufferedImage.TYPE_BYTE_GRAY);
                    this.enableControls();
                }
                else
                    this.jLabelStatus.setText("GetDeviceInfo() Error [" + ret + "]");                                
            }
            else
                this.jLabelStatus.setText("OpenDevice() Error [" + ret + "]");                                
        }
        else
            this.jLabelStatus.setText("JSGFPLib Initialization Failed");
        
        
    }//GEN-LAST:event_jButtonInitActionPerformed
    
    /** Exit the Application */
    private void exitForm(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_exitForm
        System.exit(0);
    }//GEN-LAST:event_exitForm
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
    	JSGD ventana= new JSGD();
    	ventana.setVisible(true);
    	ventana.setLocationRelativeTo(null);
    	ventana.setDefaultCloseOperation(ventana.EXIT_ON_CLOSE);
    	ventana.conexion();
    	ventana.leerTodasHuellas();
        //new JSGD().setVisible(true);
        
    }
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonCapture;
    private javax.swing.JButton jButtonCaptureR1;
    private javax.swing.JButton jButtonCaptureR2;
    private javax.swing.JButton jButtonCaptureV1;
    private javax.swing.JButton jButtonConfig;
    private javax.swing.JButton jButtonGetDeviceInfo;
    private javax.swing.JButton jButtonInit;
    private javax.swing.JButton jButtonRegister;
    private javax.swing.JButton jButtonToggleLED;
    private javax.swing.JButton jButtonVerify;
    private javax.swing.JComboBox jComboBoxDeviceName;
    private javax.swing.JComboBox jComboBoxRegisterSecurityLevel;
    private javax.swing.JComboBox jComboBoxUSBPort;
    private javax.swing.JComboBox jComboBoxVerifySecurityLevel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabelBrightness;
    private javax.swing.JLabel jLabelContrast;
    private javax.swing.JLabel jLabelDeviceID;
    private javax.swing.JLabel jLabelDeviceInfoGroup;
    private javax.swing.JLabel jLabelDeviceName;
    private javax.swing.JLabel jLabelFWVersion;
    private javax.swing.JLabel jLabelGain;
    private javax.swing.JLabel jLabelImage;
    private javax.swing.JLabel jLabelImageDPI;
    private javax.swing.JLabel jLabelImageHeight;
    private javax.swing.JLabel jLabelImageWidth;
    private javax.swing.JLabel jLabelRegisterImage1;
    private javax.swing.JLabel jLabelRegisterImage2;
    private javax.swing.JLabel jLabelRegistration;
    private javax.swing.JLabel jLabelRegistrationBox;
    private javax.swing.JLabel jLabelSecurityLevel;
    private javax.swing.JLabel jLabelSerialNumber;
    private javax.swing.JLabel jLabelSpacer1;
    private javax.swing.JLabel jLabelSpacer2;
    private javax.swing.JLabel jLabelStatus;
    private javax.swing.JLabel jLabelVerification;
    private javax.swing.JLabel jLabelVerificationBox;
    private javax.swing.JLabel jLabelVerifyImage;
    private javax.swing.JPanel jPanelDeviceInfo;
    private javax.swing.JPanel jPanelImage;
    private javax.swing.JPanel jPanelRegisterVerify;
    private javax.swing.JProgressBar jProgressBarR1;
    private javax.swing.JProgressBar jProgressBarR2;
    private javax.swing.JProgressBar jProgressBarV1;
    private javax.swing.JSlider jSliderQuality;
    private javax.swing.JSlider jSliderSeconds;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTextField jTextFieldBrightness;
    private javax.swing.JTextField jTextFieldContrast;
    private javax.swing.JTextField jTextFieldDeviceID;
    private javax.swing.JTextField jTextFieldFWVersion;
    private javax.swing.JTextField jTextFieldGain;
    private javax.swing.JTextField jTextFieldImageDPI;
    private javax.swing.JTextField jTextFieldImageHeight;
    private javax.swing.JTextField jTextFieldImageWidth;
    private javax.swing.JTextField jTextFieldSerialNumber;
}

import java.awt.BorderLayout;
import java.awt.EventQueue;

import java.util.Scanner;
import java.io.*;
import java.net.*;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.JComboBox;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import java.awt.Font;

import ezprivacy.protocol.IntegrityCheckException;
import ezprivacy.toolkit.CipherUtil;
import ezprivacy.netty.session.ProtocolException;
import ezprivacy.secret.EnhancedProfileManager;
import ezprivacy.service.authsocket.EnhancedAuthSocketClient;
import ezprivacy.service.register.EnhancedProfileRegistrationClient;
import ezprivacy.toolkit.CipherUtil;
import ezprivacy.toolkit.EZCardLoader;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;



public class Storage_System<E> extends JFrame {

	private JPanel contentPane;
	private JTextField Search_field;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args){
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Storage_System frame = new Storage_System();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 * @param id 
	 */
	public Storage_System(String id,String password) {

		byte[] main_iv = "0101010101010101".getBytes();
		
		/*
		 * 	get the main key
		 */
		byte[] main_key = new byte[16];
		byte[] pass = password.getBytes();
		if(pass.length<=16){
			for(int i=0;i<pass.length;i++)
				main_key[i]=pass[i];
			if(main_key.length<16){
				for(int i=main_key.length;i<16;i++)
					main_key[i]=1;
			}
		}
		else{
			for(int i=0;i<16;i++)
				main_key[i]=pass[i];
		}
		
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle("F74035047 Super Storage System!");
		setBounds(100, 100, 511, 412);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(167, 26, 293, 317);
		contentPane.add(scrollPane);
		JList list = new JList();
		scrollPane.setViewportView(list);
		
		/*
		 *  Refresh list
		 */
		JButton Refresh = new JButton("Refresh");
		Refresh.setFont(new Font("Tahoma", Font.PLAIN, 15));
		Refresh.setBounds(31, 308, 114, 35);
		Refresh.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try{
					Socket s = new Socket("localhost",8080);
					DataOutputStream out = new DataOutputStream(s.getOutputStream());
					out.writeInt(2);
					out.writeUTF(id);
				
					out.close();
					s.close();
				
					get_list(list);
				}catch ( Exception e ){
					e.printStackTrace();
				}
			}
		});
		contentPane.add(Refresh);
		
		/*
		 * Delete item
		 */
		JButton Delete = new JButton("Delete");
		Delete.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String delete_item = (String)list.getSelectedValue();
				int action = JOptionPane.showConfirmDialog(null,"Do You really want to delete "+delete_item+"?","Delete",JOptionPane.YES_NO_OPTION);
				if(action==0){
					try{
						Socket s = new Socket("localhost",8080);
						DataOutputStream out = new DataOutputStream(s.getOutputStream());
						out.writeInt(3);
						out.writeUTF(id);
						out.flush();
						out.close();
						s.close();
						Delete_item(delete_item,id);
						
					}catch (IOException e1){
			            e1.printStackTrace();
			        }	
				}
			}
		});
		Delete.setFont(new Font("Tahoma", Font.PLAIN, 14));
		Delete.setBounds(31, 262, 114, 35);
		contentPane.add(Delete);
		
		
		/*
		 *   Download item	
		 */
		JButton Download = new JButton("Download");
		Download.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String download_item = (String)list.getSelectedValue();
				int action;
				if(download_item!=null){
					action = JOptionPane.showConfirmDialog(null,"Download the item "+download_item+"?","Download",JOptionPane.YES_NO_OPTION);
					if(action==0){
						try{
							File id_file = new File(id);
							String sk_file = new String(id_file.getAbsolutePath()+"\\"+download_item);
							File sk_text = new File(sk_file);
							FileInputStream in_sk = new FileInputStream(sk_text);
							byte[] sk = new byte[16];
							byte[] iv = new byte[16];
							byte[] read = new byte[65];
							byte[] sk_iv = new byte[64];
							int i;
							int textlength = 0;
							while(in_sk.available()>0){
								int readin = in_sk.read(read);
								textlength = read[64];
								
								for(i=0;i<64;i++)
									sk_iv[i]=read[i];
								byte[] dec_skiv = CipherUtil.authDecrypt(main_key,main_iv,sk_iv);	//decrypt the sk and iv
								
								/*
								 * 	get sk and iv
								 */
								
								for(i=31;i>=16;i--)
									iv[i-16]=dec_skiv[i];
								for(i=15;i>=0;i--)
									sk[i]=dec_skiv[i];
								
								//connect to server and get the download item
								Socket s = new Socket("localhost",8080);
								DataOutputStream out = new DataOutputStream(s.getOutputStream());
								out.writeInt(1);
								out.writeUTF(id);
								out.flush();
								out.close();
								s.close();
							}
							in_sk.close();
							Download d = new Download(download_item,sk,iv,textlength);
							
						}catch(Exception e){ 
				            e.printStackTrace(); 
				        } 
					}
				}
				else
					JOptionPane.showMessageDialog(null, "Please Select a item to download");
			}
		});
		Download.setFont(new Font("Tahoma", Font.PLAIN, 14));
		Download.setBounds(31, 216, 114, 35);
		contentPane.add(Download);
		
		
		
		/*
		 *   Upload item
		 */
		JButton Upload = new JButton("Upload");
		Upload.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0){
				JFileChooser fc_up = new JFileChooser();
				fc_up.setDialogTitle("Upload");
				fc_up.setApproveButtonText("Upload");
				int action = fc_up.showOpenDialog(null);	//get action
				if(action == JFileChooser.APPROVE_OPTION){
					File f = fc_up.getSelectedFile();
					File id_file = new File(id);
					id_file.mkdir();
					int mode=1;
					if(id_file.isDirectory()){
						String[] ss = id_file.list();
						for(int i=0;i<ss.length;i++){
							if((f.getName()).equals(ss[i])){
								mode=0;
								break;
							}
						}
					}
					if(mode==0){
						int act = JOptionPane.showConfirmDialog(null,"The item \""+f.getName()+"\" is already existed. Do you want to replace it?","Replace",JOptionPane.YES_NO_OPTION);
						if(act==0){
							Upload u = new Upload(f,id,main_key,main_iv);
						}
					}
					else{
						Upload u = new Upload(f,id,main_key,main_iv);
					}
					
				}
		}});
		Upload.setFont(new Font("Tahoma", Font.PLAIN, 14));
		Upload.setBounds(31, 170, 114, 35);
		contentPane.add(Upload);
		
		Search_field = new JTextField();
		Search_field.setBounds(14, 81, 143, 27);
		contentPane.add(Search_field);
		Search_field.setColumns(10);
		
		JButton Search = new JButton("Search");
		Search.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String item = Search_field.getText();
				ListModel model = list.getModel();
				DefaultListModel listModel = (DefaultListModel) list.getModel();
				for(int i=0;i<model.getSize();i++){
					if(item.equals(model.getElementAt(i))){
						list.setSelectedIndex(i);
					}
				}
			}
		});
		Search.setFont(new Font("Tahoma", Font.PLAIN, 15));
		Search.setBounds(31, 124, 114, 35);
		contentPane.add(Search);
		
		JLabel ID_label = new JLabel("");
		ID_label.setFont(new Font("Tahoma",Font.BOLD,15));
		ID_label.setText("ID: "+id);
		ID_label.setBounds(14, 28, 143, 35);
		contentPane.add(ID_label);
	}

	public Storage_System() {
		// TODO Auto-generated constructor stub
	}
	
	/*
	 * 		List function
	 */
	void get_list(JList list){
		try{
			Socket s = new Socket("localhost",5888);
			DataInputStream in = new DataInputStream(s.getInputStream());
			DefaultListModel DLM = new DefaultListModel();
			int check=0;
			while(true){
				String filename = new String(in.readUTF());
				if("-1".equals(filename))
					break;
				else{
					check=1;
					DLM.addElement(filename);
					list.setModel(DLM);
				}
			}
			list.setModel(DLM);
			in.close();
			s.close();
		}catch(Exception e){ 
            e.printStackTrace(); 
        } 
	}
	
	/*
	 * 		Delete function
	 */
	void Delete_item(String delete_item,String id){
		try{
			Socket ss = new Socket("localhost",3333);
			File id_file = new File(id);
			String sk_file = new String(id_file.getAbsolutePath()+"\\"+delete_item);
			File sk_text = new File(sk_file);
			DataOutputStream output = new DataOutputStream(ss.getOutputStream());
			output.writeUTF(delete_item);
			output.flush();
			ss.close();		
			sk_text.delete();
			JOptionPane.showMessageDialog(null,delete_item+" is Deleted!");
		}catch (IOException e){
            e.printStackTrace();
        }
	}
	
}

class Download{
	Download(String filename,byte[] sk,byte[] iv,int textlength){
		JFileChooser j = new JFileChooser();
		j.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		j.setDialogTitle("Download");
		j.setApproveButtonText("OK");
		int action = j.showOpenDialog(null);
		if(action==0){
			try{
				Socket s = new Socket("localhost",5888);
				DataOutputStream output = new DataOutputStream(s.getOutputStream());
				output.writeUTF(filename);
				output.flush();
				s.close();
				String path = j.getSelectedFile().toString();
				Thread.yield();
				go(path,filename,sk,iv,textlength);
			}catch (IOException e){
	            e.printStackTrace();
	        }
		}
	}
	void go(String path,String filename,byte[] sk,byte[] iv,int textlength){
		byte[] text = new byte[33];
		byte[] ciphertext = new byte[32];
		try{
			Socket s = new Socket("localhost",5777);
			BufferedInputStream in = new BufferedInputStream(s.getInputStream());
			FileOutputStream out = new FileOutputStream(path+"\\"+filename); 
			int readin;
			while((readin = in.read(text))!=-1){
				for(int i=0;i<32;i++)
					ciphertext[i]=text[i];
				try{
					byte dtext[]=CipherUtil.authDecrypt(sk,iv,ciphertext);
					if(text[32]==0)
						out.write(dtext);
					else{
						for(int i=0;i<textlength;i++)
							out.write(dtext[i]);
					}
				}catch (IntegrityCheckException e) {
					e.printStackTrace();
				}
			}
			out.flush();
			in.close();
			out.close();
			s.close();
			JOptionPane.showMessageDialog(null,filename+" Download successful!");
		}catch (IOException e){
            e.printStackTrace();
        }
	}
}

class Upload{
	Upload(File f,String id,byte[] main_key,byte[] main_iv){
		try{
			/*
			 * 	send the ID and mode to server
			 */
			Socket s = new Socket("localhost",8080);
			DataOutputStream out = new DataOutputStream(s.getOutputStream());
			out.writeInt(0);
			out.writeUTF(id);
			out.close();
			s.close();
			
			/*
			 * 		Generate session key and upload
			 */
			EnhancedProfileManager profile = EZCardLoader.loadEnhancedProfile(new File("client.card"), "00000");
			EnhancedAuthSocketClient client = new EnhancedAuthSocketClient(profile);
		
			client.connect("localhost", 5471);
			client.doEnhancedKeyDistribution();
			client.doRapidAuthentication();
		
			EZCardLoader.saveEnhancedProfile(profile, new File("client.card"), "00000");

			byte[] k = client.getSessionKey().getKeyValue();
			byte[] sk = CipherUtil.copy(k, 0, CipherUtil.KEY_LENGTH);
			byte[] iv = CipherUtil.copy(k, CipherUtil.KEY_LENGTH, CipherUtil.BLOCK_LENGTH);
			
			client.close();
				
			Client_Upload c = new Client_Upload(sk,iv,f,id,main_key,main_iv);
			
		}catch ( ProtocolException e ) {	
			System.out.println("[ProtocolException]" + e.getLocalizedMessage());
			if(e.getLocalizedMessage().contains("NoSuchIdentifierException"))
				System.out.println("Yours or server's EZCard may be outdated. Please consider register a new one or contact with server.");
		}catch ( Exception e ){
			e.printStackTrace();
		}
	}
}

class Client_Upload{	
	Client_Upload(byte[] sk,byte[] iv,File f,String id,byte[] main_key,byte[] main_iv){
		byte[] text = new byte[8];
		String file = new String(f.toString());
		String filename = f.getName();
		int readin=0;
		try{
			Socket s = new Socket("localhost",5888);
			DataOutputStream out = new DataOutputStream(new BufferedOutputStream(s.getOutputStream()));
			BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));
			out.writeUTF(filename);		
			while(in.available()>0){
				readin = in.read(text);
				out.writeInt(readin);
				byte[] ciphertext = CipherUtil.authEncrypt(sk, iv, text);
				out.write(ciphertext);
				Thread.yield();
			}
			out.close();
			in.close();
			s.close();
			get_key(f,id,readin,main_key,main_iv);
		}catch(Exception e){ 
            e.printStackTrace(); 
        } 
	}
	
	void get_key(File f,String id,int readin,byte[] main_key,byte[] main_iv){
		try{
			Socket ss = new Socket("localhost",5880);
			DataInputStream in = new DataInputStream(ss.getInputStream());		
			byte[] sk_iv = new byte[32];
			in.read(sk_iv);		//get the sk and iv from server
			in.close();
			ss.close();

			File id_file = new File(id);
			String sk_file = new String(id_file.getAbsolutePath()+"\\"+f.getName());
			File sk_text = new File(sk_file);
			sk_text.createNewFile();
			DataOutputStream out_sk = new DataOutputStream(new FileOutputStream(sk_text));
			byte[] cipher_skiv = CipherUtil.authEncrypt(main_key , main_iv , sk_iv); 	//encrypt the sk and iv
			out_sk.write(cipher_skiv);
			out_sk.write(readin);
			out_sk.flush();
			out_sk.close();
			JOptionPane.showMessageDialog(null,f.getName()+" Upload successful!");
		}catch(Exception e){ 
            e.printStackTrace(); 
        } 
	}
}
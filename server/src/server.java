import java.util.Scanner;
import java.io.*;
import java.net.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.util.Scanner;
import ezprivacy.netty.session.ProtocolException;
import ezprivacy.secret.EnhancedProfileManager;
import ezprivacy.service.authsocket.AuthSocketServer;
import ezprivacy.service.authsocket.EnhancedAuthSocketServerAcceptor;
import ezprivacy.toolkit.CipherUtil;
import ezprivacy.toolkit.EZCardLoader;
import ezprivacy.protocol.IntegrityCheckException;
import ezprivacy.toolkit.CipherUtil;
import javax.swing.JOptionPane;

public class server
{
	public static void main( String[] args ){
	while(true){
		try {
			System.out.println("===== Server Start =====");
			
			//get the ID and mode
			ServerSocket s1;
			s1 = new ServerSocket(8080);
			Socket sock = s1.accept();			
			DataInputStream in = new DataInputStream(sock.getInputStream());
			int mode = in.readInt();
			String id = new String(in.readUTF());
			in.close();
			sock.close(); 
			s1.close();
			
			//Upload
			if(mode==0){
				EnhancedProfileManager profile = EZCardLoader.loadEnhancedProfile(new File("server.card"), "00000");
			
				// Initialize authsocket acceptor
				EnhancedAuthSocketServerAcceptor serverAcceptor = new EnhancedAuthSocketServerAcceptor(profile);
			
				// bind to some port
				serverAcceptor.bind(5471);
	
				// get accepted socket
				AuthSocketServer server = serverAcceptor.accept();
				System.out.println("Accepted a client from " + server.getRemoteAddress());
			
				// wait for MAKD execution
				server.waitUntilAuthenticated();
	
				// save card back
				EZCardLoader.saveEnhancedProfile(profile, new File("server.card"), "00000");

				// get KD and RA result
				byte[] k = server.getSessionKey().getKeyValue();
				byte[] sk = CipherUtil.copy(k, 0, CipherUtil.KEY_LENGTH);
				byte[] iv = CipherUtil.copy(k, CipherUtil.KEY_LENGTH, CipherUtil.BLOCK_LENGTH);
				
				server.waitUntilClose();
				server.close();
				serverAcceptor.close();

				Upload s = new Upload(sk,iv,id);
				
			}
			
			//Download
			else if(mode==1){
				Download dl = new Download(id);
			}
			
			//Refresh List
			else if(mode==2){
				List list =new List(id);
			}
			
			//Delete item
			else if(mode==3){
				Delete delete = new Delete(id);
			}
			
			//Register a new ID
			else if(mode==4){
				Register r = new Register(id);
			}
			
			//Login 
			else if(mode==5){
				Login lll = new Login(id);
			}
			
		} catch ( ProtocolException e ) {
			System.out.println("[ProtocolException]" + e.getLocalizedMessage());
			if(e.getLocalizedMessage().contains("NoSuchIdentifierException"))
				System.out.println("Yours or client's EZCard may be outdated. Please consider register a new one.");
			
		} catch ( Exception e ){
			System.out.println(e);
		}
	}
	}
}

class Login{
	Login(String id){
	try{
		ServerSocket s;
		s = new ServerSocket(6666);
		Socket sock = s.accept();
		BufferedInputStream in = new BufferedInputStream(new DataInputStream(sock.getInputStream()));
		DataOutputStream out = new DataOutputStream(sock.getOutputStream());
		byte[] text = new byte[48];
		byte[] iv = "1010555577778888".getBytes();
		byte[] pass = new byte[16];
		try{
			String file = "ID_PASS";
			File id_file = new File(file);
			BufferedInputStream input = new BufferedInputStream(new FileInputStream(id_file.getAbsolutePath()+"\\"+id+".txt"));
			in.read(pass);		//get the password that client input
			int check=0;
			int readin;
			while((readin=input.read(text))!=-1){
				try{
					byte[] dec = CipherUtil.authDecrypt(pass,iv,text);	
					for(int i=0;i<16;i++){		//check the password is correct or not
						if(pass[i]==dec[i]){
							check++;
						}
					}
					if(check==16){
						out.writeInt(1);		//password correct
						out.flush();
						input.close();
						in.close();
						out.close();
						sock.close();
						s.close();
					}
					else{
						out.writeInt(0);		//password incorrect
						out.flush();
						input.close();
						in.close();
						out.close();
						sock.close();
						s.close();
					}
				}catch (IntegrityCheckException e) {
					out.writeInt(0);		//password incorrect
					out.flush();
					input.close();
					in.close();
					out.close();
					sock.close();
					s.close();
				}
			}
			
			
		}catch (IOException e){
			out.writeInt(0);			//password or ID incorrect
			out.flush();
			in.close();
			out.close();
			sock.close();
			s.close();
        }
	}catch (IOException e){
           e.printStackTrace();			//password or ID incorrect
    }
	}
}

class Register{
	Register(String id){
		ServerSocket s;
		try{
			s = new ServerSocket(2222);
			Socket sock = s.accept();
			String file = "ID_PASS";
			File id_file = new File(file);
			id_file.mkdir();
			String file_path = new String(id_file.getAbsolutePath()+"\\"+id+".txt");
			DataOutputStream out = new DataOutputStream(sock.getOutputStream());
			BufferedInputStream in = new BufferedInputStream(sock.getInputStream());
			int check=1;
			if(id_file.isDirectory()){			//checking the ID is exist or not
				String[] ss = id_file.list();
				String y = new String(id+".txt");
				for(int i=0;i<ss.length;i++){
					if(y.equals(ss[i])){			//ID is existed
						check=0;
						break;
					}
				}
			}
			if(check==0){				//Fail register 
				out.writeInt(0);
				in.close();
				sock.close();
				s.close();
			}
			else{						//Register successful
				FileOutputStream output = new FileOutputStream(file_path);
				out.writeInt(1);
				byte[] pass = new byte[48];
				int readin;
				while((readin = in.read(pass))!=-1);
				output.write(pass);
				output.flush();
				output.close();
				in.close();
				sock.close();
				s.close();
			}
		}catch (IOException e){
            e.printStackTrace();
        }
		
	}
}

class Delete{
	Delete(String id){
		ServerSocket s;
		try{
			File server = new File(id);
			s = new ServerSocket(3333);
			Socket sock = s.accept();
			DataInputStream in = new DataInputStream(sock.getInputStream());
			String delete_item = in.readUTF();
			String delete_path = new String(server.getAbsolutePath()+"\\"+delete_item);
			File delete_file = new File(delete_path);
			delete_file.delete();
			in.close();
			sock.close();
			s.close();
			
		}catch (IOException e){
            e.printStackTrace();
        }
	}
}

class Download{
	Download(String id){
		ServerSocket s;
		try{
			File server = new File(id);
			s = new ServerSocket(5888);		//download port
			Socket sock = s.accept();
			BufferedInputStream input = new BufferedInputStream(new DataInputStream(sock.getInputStream()));
			String d = new String(new DataInputStream(input).readUTF());	//get the fileName
			input.close();
			sock.close();
			s.close();
			go(d,server);
		}catch (IOException e){
            e.printStackTrace();
        }
	}
	void go(String d,File server){
		ServerSocket s1;
		byte[] text = new byte[32];
		byte[] check = new byte[33];	
		check[32]=0;	
		try{
			s1 = new ServerSocket(5777);
			Socket sock = s1.accept();
			FileInputStream in = new FileInputStream(server.getAbsolutePath()+"\\"+d);
			BufferedOutputStream out = new BufferedOutputStream(sock.getOutputStream());
			while(in.available()>0){
				int readin = in.read(text);
				for(int i=0;i<32;i++)
					check[i]=text[i];
				if(in.available()<=0)	//read done
					check[32]=1;
				else check[32]=0;
				out.write(check);
				Thread.yield();
			}
			out.flush();
			in.close();
			out.close();
			sock.close();
			s1.close();	
		}catch (IOException e){
            e.printStackTrace();
        }
	}
}

class List{
	List(String id){
		ServerSocket s;
		try{
			s = new ServerSocket(5888);		//list port
			File id_file = new File(id);
			Socket sock = s.accept();
			DataOutputStream out = new DataOutputStream(sock.getOutputStream());
			if(id_file.isDirectory()){
				String[]ss = id_file.list();
				for(int k=0;k<ss.length;k++){		
					out.writeUTF(ss[k]);
				}
				out.writeUTF("-1");		//list ended condition
			}
			else
				out.writeUTF("-1");		
			out.close();
			sock.close(); 
			s.close();
		}catch (IOException e){
            e.printStackTrace();
        }
	}
}

class Upload{
	Upload(byte[] sk,byte[] iv,String ID){
		ServerSocket s;
		byte[] text = new byte[32];
		try{
			s = new ServerSocket(5888);			//upload port
			File server = new File(ID);
			server.mkdir();
            Socket sock = s.accept();

			BufferedInputStream inputStream = new BufferedInputStream(sock.getInputStream());	
			String fileName = new DataInputStream(inputStream).readUTF();		//get the file name
			String file = new String(server.getAbsolutePath()+"\\"+fileName);
			BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(file));  
            while(inputStream.available()>0){
				int textLength = new DataInputStream(inputStream).readInt();		//get the text length
				inputStream.read(text);		//start read
				outputStream.write(text);
				Thread.yield();
            }  			
			outputStream.close();                
            inputStream.close();  
			sock.close(); 
			s.close();
		}catch (IOException e){
            e.printStackTrace();
        }
	}
	
}

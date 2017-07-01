import javax.swing.*;
import java.awt.dnd.*;
import java.awt.Color;
import java.awt.datatransfer.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;


public class Main {
    public static JButton btnProcess;

    public static void main(String[] args) {

        //create a window
        JFrame frame = new JFrame("picManager");
        frame.setLayout(null); //no layout
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //default close action

        //name of all labels
        String[] lblNames={"Normal Images",".private",".misc.private"};

        for(int i=0;i<3;i++) {
            JLabel lblTmp = new JLabel("  "+lblNames[i]);//create a label
            frame.getContentPane().add(lblTmp);//add to jframe
            lblTmp.setVisible(true);//make label visible
            lblTmp.setBounds(100*i+10*(i+1), 10, 100, 100); //set positions according to index
            lblTmp .setBorder(BorderFactory.createLineBorder(Color.BLUE, 1)); //set border of label

            DropTargetHandler dthLblTmp = new DropTargetHandler();//Drop handler
            dthLblTmp.type = i;//set type according to index
            new DropTarget(lblTmp, dthLblTmp);//attach drop handler
        }


        btnProcess = new JButton("New Stock ( Current : 10000 )" );
        frame.getContentPane().add(btnProcess);
        btnProcess.setVisible(true);
        btnProcess.setBounds(110,120,btnProcess.getPreferredSize().width,btnProcess.getPreferredSize().height);
        btnProcess.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DropTargetHandler.normalSetCount((DropTargetHandler.normalGetCount() / 100) * 100 + 100);
            }
        });

        DropTargetHandler.normalGetCount();//refresh button name

        //Draw frame then set size of frame and make it visible
        frame.setSize(400,200);
        frame.setVisible(true);
    }
    //set label text with count
    public static void lblSetCount(int index){
        btnProcess.setText("New Stock ( Current : "+ index +")");
    }

}

class log{
    public static void print(String message){
        System.out.print(message);
    }
}




class DropTargetHandler implements DropTargetListener {
    /*
    0 : Normal Pic
    1 : Private Pic
    2 : Private misc
    3 : Audio.Call
     */
    public int type;

    //root directory which contains all our dir
    public static String dirRoot="D:\\Dropbox\\Exec\\init";

    //array of all directories
    private static  String[] dirList={"Pic.forMe","Pic.forMe\\ .private","Pic.forMe\\.misc.private"};

    //basic regex checks if . doesnot contains \ after it. Then it is a extension
    private String extRegex="\\.(?=[^\\.]+$)";


    @Override
    public void drop(DropTargetDropEvent dtde) {
        Transferable tr = dtde.getTransferable();
        if (tr.getTransferDataFlavors()[0].isFlavorJavaFileListType()) {
            dtde.acceptDrop(DnDConstants.ACTION_LINK);
            try {
                //use java.util.list as wing class contains a List too.
                java.util.List list = (java.util.List) tr.getTransferData(tr.getTransferDataFlavors()[0]);

                boolean changed=false; //if any file name change has been done
                int index = normalGetCount(); //get current stored index
                //if(type==0)index = (index/100)*100+100;

                for (int j = 0; j < list.size(); j++) {
                    File file=((File)list.get(j));
                    String[] parts=file.getName().split(extRegex);//split with regex 0 index contains name & 1 index extnsion of file
                    String ext=parts[1]; //get file extension

                    //if it is an image
                    if(stringContainsItemFromList(new String[]{"jpg","jpeg","png","gif"},ext)) {
                        int fName = -1;
                        try {
                            //try to parse name as int, if successful it can be one of our file
                            fName = Integer.parseInt(parts[0]);
                        } catch (Exception e) {
                            //e.printStackTrace();
                        } finally {
                            log.print(fName+"");
                            if (!(fName > -1 && fName <= index) || !file.getPath().startsWith(dirRoot)) {
                                //if not in directory, filename not an integer, even if it is an integer it should not be greater than -1 & less than index.
                                log.print("Rename.\n");
                                changed = true;
                                file.renameTo(new File(dirRoot + "\\" + dirList[type] + "\\" + ++index + "." + ext));
                            }else if(file.getParent().startsWith(dirRoot)){
                                //if in main directory. Just move
                                //log.print(dirRoot+"\\"+dirList[type]+"\\" + file.getName());
                                file.renameTo(new File(dirRoot+"\\"+dirList[type]+"\\" + file.getName()));
                            } else {
                                log.print("Ignore.\n");
                            }
                        }
                    } else if(stringContainsItemFromList(new String[]{"amr","mp3","wav"},ext)) {
                        String[] in = parts[0].split("_");
                        log.print(dirRoot+"\\Audio\\" + in[4]+"."+in[3]+"."+in[2]+"_" +in[5]+"."+in[6] +"."+ ext +"\n");
                        file.renameTo(new File(dirRoot+"\\Audio\\" + in[4]+"."+in[3]+"."+in[2]+"_" +in[5]+"."+in[6] +"."+ ext ));
                    }

                   // normalFloatToInteger(parts,parent,ext,file);
                }

                //if file is dropped in an image & anyname change has occurred save current index
                if(type<3 && changed) normalSetCount(index);

            } catch (UnsupportedFlavorException e) {
                log.print("Unsupported Flavour");
            } catch (IOException e) {
                e.printStackTrace();
            }

            dtde.dropComplete(true);
        }
    }
    //get stored value from .nomedia
    public static int normalGetCount() {
        try{
            BufferedReader reader =new BufferedReader(new FileReader(new File( dirRoot +"\\Pic.forMe\\.nomedia")));
            int index=Integer.parseInt(reader.readLine());
            reader.close();
            Main.lblSetCount(index);
            return index;
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return 0;
    }

    //save index in .nomedia file
    public static void  normalSetCount(int index){
        try {
            BufferedWriter writer =new BufferedWriter(new FileWriter(new File(dirRoot +"\\Pic.forMe\\.nomedia"),false));
            writer.write(Integer.toString(index));
            writer.close();
            Main.lblSetCount(index);
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    //returns true if haysack string array contains needle string
    public static boolean stringContainsItemFromList( String[] haysack,String needle)
    {
        for(int i =0; i < haysack.length; i++)
            if(haysack[i].compareTo(needle)==0)
                return true;

        return false;
    }
    void normalFloatToInteger(String[] parts,String parent,String ext,File file ){
        parts = parts[0].split(extRegex);
        if(Integer.parseInt(parts[1])<10 )
            file.renameTo(new File(parent + "\\" + parts[0] + "0" + parts[1] + "." + ext));
        else
            file.renameTo(new File(parent + "\\" + parts[0] +  parts[1] + "." + ext));
    }


    @Override
    public void dragEnter(DropTargetDragEvent dtde) {

    }

    @Override
    public void dragOver(DropTargetDragEvent dtde) {

    }

    @Override
    public void dropActionChanged(DropTargetDragEvent dtde) {
    }

    @Override
    public void dragExit(DropTargetEvent dte) {

    }

}

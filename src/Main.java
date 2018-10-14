import java.io.*;
import java.util.*;


public class Main {

    public static void main(String args[]) throws FileNotFoundException {

        int[] dirs = getSubDirs("/proc/");
        System.out.println(Arrays.toString(dirs));

        Scanner scan = new Scanner(new File("/proc/net/tcp"));
        scan.nextLine();
        HashSet<Integer> tcpSockets = new HashSet<>();
        while(scan.hasNext()){
            String s = scan.nextLine().trim();
            Scanner hacky = new Scanner(s);
            int count = 0;
            while(hacky.hasNext() && count < 9){
                hacky.next();
                count++;
            }
            Integer tcp = Integer.parseInt(hacky.next());
            tcpSockets.add(tcp);
        }

        // Socket, Processes using them
        HashMap<Integer, HashSet<Integer>> mappyMap = new HashMap<>();

        for(int i : dirs) {
            StringBuilder b = new StringBuilder("ls -l /proc/");
            // matching only "socket:[" + any number
            b.append(i).append("/fd | grep -o socket:[[][0-9]*");
            try {
                // have to run as a script, can't just pass grep to java runtime process
                String[] ret = (bashExecute("/bin/sh", "-c", b.toString()));

                for(String s : ret){
                    Integer socketNumber = Integer.parseInt(s.substring(8));
                    HashSet<Integer> settySet = mappyMap.get(socketNumber);
                    if(settySet == null)
                    {
                        settySet = new HashSet<>();
                        mappyMap.put(socketNumber, settySet);
                    }
                    settySet.add(i);

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        for(Integer tcpSock : tcpSockets){
            if(mappyMap.containsKey(tcpSock)){
                System.out.println("TCP: " + tcpSock);
                System.out.println("\t" + mappyMap.get(tcpSock));
            }
        }

        /*
        for(Integer socket: mappyMap.keySet()){
            System.out.println("Socket: " + socket);
            System.out.println("\t" + mappyMap.get(socket));
        }*/
    }

    public static String[] bashExecute(String ... command) throws Exception {
        ArrayList<String> output = new ArrayList<>();
        Process p = Runtime.getRuntime().exec(command);
        p.waitFor();
        BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));

        String next = null;
        while((next = in.readLine()) != null){
            output.add(next);
        }
        in.close();
        p.destroy();
        return output.toArray(new String[0]);
    }

    // gets the integer subdirectories only
    public static int[] getSubDirs(String dir)
    {

        File file = new File(dir);
        String[] directories = file.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return dir.isDirectory() && name.matches("-?\\d+");
            }
        });
       return Arrays.stream(directories).mapToInt((String s) -> (Integer.parseInt(s))).toArray();
    }
}

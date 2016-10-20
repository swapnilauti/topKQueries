import java.io.*;
import java.util.*;
import topkutils.*;

class IdAttribute{
	double id;
	double attribute;
	
	public IdAttribute(Double id, Double attribute) {
		this.id = id;
		this.attribute = attribute;
	}
	
	public double getId() {
		return id;
	}

	public double getAttribute() {
		return attribute;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return String.valueOf(this.id) + " " + String.valueOf(this.attribute);
	}
}

class IdAttributeSort implements Comparator<IdAttribute>{

	@Override
	public int compare(IdAttribute o1, IdAttribute o2) {
		return o1.attribute<o2.attribute?1:o1.attribute>o2.attribute?-1:0;
	}
	
}

class Tuple implements Comparable<Tuple>{
	private Double id,attribute[],threshold;

	public Tuple(Double id, Double[] attribute,Double[] V) {
		super();
		this.id = id;
		this.attribute = attribute;
		computeThreshold(V);
	}
	public Tuple(Double id, Double[] attribute) {
		super();
		this.id = id;
		this.attribute = attribute;
		threshold = 0d;		
	}
	
	public boolean equals(Object o){
		if(o instanceof Tuple){
			return this.id==((Tuple)o).id;
		}
		return false;
	}
	public int compareTo(Tuple o){
		return this.threshold.compareTo(o.threshold);
	}

	public Double computeThreshold(Double V[]){
		threshold = 0d;
		int n = V.length;
		for(int i=0;i<n;i++){
			threshold+=attribute[i]*V[i];
		}
		return threshold;
	}

	public Double getThreshold() {
		return threshold;
	}
	@Override
	public String toString() {
		String s = new String();
		s=s.concat(""+id);
		for(int i=0;i<attribute.length;i++){
			s=s.concat(" "+attribute[i]);
		}
		s=s.concat(" "+threshold);		
		return s;
	}
}


public class TopK {
	List<List<IdAttribute>> outerList = new ArrayList<List<IdAttribute>>();
	List<BTree<Double, Double>> btreeList = new ArrayList<BTree<Double,Double>>();
	List<Tuple> table = new ArrayList<Tuple>();
	Double V[];
	int n,rows,k;
	public TopK(int n, int k, Double V[]){
		this.n = n;
		this.k = k;
		this.V = V;
	}
	
	private void pqInsert(PriorityQueue<Tuple> pq, Tuple t){
		if(pq.size()<k){
			pq.offer(t);
		}
		else{
			if(t.getThreshold()>pq.peek().getThreshold()){
				pq.poll();
				pq.offer(t);
			}
		}
	}
	
	private Tuple createTuple(Double id){
		Double attribute[] = new Double[n];
		int i=0;
		for(Iterator<BTree<Double, Double>> it = btreeList.iterator();it.hasNext();){
			attribute[i++] = it.next().get(id);
		}
		return new Tuple(id,attribute,V);
	}
	
	private Double computeThreshold(Double attribute[]){
		Double threshold = 0d;
		int n = V.length;
		for(int i=0;i<n;i++){
			threshold+=attribute[i]*V[i];
		}
		return threshold;
	}
	
	private boolean indexCreation(String inputFile){		
		BufferedReader inputBufferedReader = null;
		FileReader fr = null;
		String inputLine = null;
		String splitRegex = null;
		
		
		try {
			fr = new FileReader(inputFile);
			inputBufferedReader = new BufferedReader(fr);
			String headerLine = inputBufferedReader.readLine();
			int headerSplitLength = headerLine.split(",").length;
			for(int i=0;i<headerSplitLength-1;i++){
				List<IdAttribute> tempList = new ArrayList<IdAttribute>();
				outerList.add(tempList);
			}
			
			
			rows = 0;
			for(;(inputLine=inputBufferedReader.readLine())!=null;rows++) {
				String[] lineSplit = inputLine.split(splitRegex);
				Double attrValues[] = new Double[n];
				for(int i=0;i<outerList.size();i++){					
					attrValues[i] = Double.valueOf(lineSplit[i+1]);
					outerList.get(i).add(new IdAttribute(Double.valueOf(lineSplit[0]),Double.valueOf(lineSplit[i+1])));
				}				
				table.add(new Tuple(Double.valueOf(lineSplit[0]), attrValues));
				
			}
			
			for(List<IdAttribute> tempList:outerList){
				Collections.sort(tempList,new IdAttributeSort());
				BTree<Double, Double> tempBtree = new BTree<Double, Double>();
				for(IdAttribute idAttribute: tempList){
					tempBtree.put(idAttribute.id, idAttribute.attribute);
				}
				btreeList.add(tempBtree);
			}
			
			
		} catch (FileNotFoundException e) {
			System.out.println("FileNotFoundException in reading input file");
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			System.out.println("IOException in reading line from input file");
			e.printStackTrace();
			return false;
		} finally {
			if(inputBufferedReader != null){
				try {
					inputBufferedReader.close();
				} catch (IOException e) {
					System.out.println("IOException in closing inputBufferedReader");
					e.printStackTrace();
				}
			}
			try{
				fr.close();
			}
			catch(Exception e){
				
			}
		}		
		return true;
	}
	
	public void init(String fileName){
		if(!indexCreation(fileName)){
			System.exit(0);
		}
	}
	
	public Stack<Tuple> naiveAlgo(){
		PriorityQueue<Tuple> result = new PriorityQueue<Tuple>();
		
		Tuple cur = null;
		for(int i=0;i<rows;i++){
			cur = table.get(i);
			//System.out.println(cur);
			cur.computeThreshold(V);
			//System.out.println(cur);
			pqInsert(result,cur);
		}
		Stack<Tuple> s = new Stack<Tuple>();
		while(!result.isEmpty()){			
			s.push(result.poll());
		}
		return s;
	}
	
	public Stack<Tuple> thresholdAlgo(){

		PriorityQueue<Tuple> result = new PriorityQueue<Tuple>();		
		IdAttribute curRecord = null;
		Double threshold = 0d;
		HashSet<Double> newKeys = new HashSet<Double>();
		Double attrValues[] = new Double[n];
		//Threshold Algorithm 
		for(int i=0;i<rows;i++){
			if(result.size()>=k && result.peek().getThreshold()>= threshold){
				break;
			}
			int j=0;
			for(Iterator<List<IdAttribute>> listIterator = outerList.iterator();listIterator.hasNext();j++){
				curRecord = listIterator.next().get(i);
				attrValues[j] = curRecord.getAttribute();				
				newKeys.add(curRecord.getId());
			}
			threshold = computeThreshold(attrValues);
			for(Iterator<Double> newKeysIterator = newKeys.iterator();newKeysIterator.hasNext();){
				Double id = newKeysIterator.next();
				Tuple curTuple = createTuple(id);
				if(!result.contains(curTuple)){
					if(result.size()>=k){
						if(result.peek().getThreshold()<curTuple.getThreshold()){
							result.poll();
							result.offer(curTuple);
						}
					}
					else{
						result.offer(curTuple);
					}
				}
			}
		}	
		Stack<Tuple> s = new Stack<Tuple>();
		while(!result.isEmpty()){			
			s.push(result.poll());
		}
		return s;
	}
	
	public void printStack(Stack<Tuple> s){
		while(!s.isEmpty()){
			System.out.println(s.pop());
		}
	}
		
	public void printTable(){
		for(Iterator<Tuple> it = table.iterator();it.hasNext();){
			System.out.println(it.next());
		}
	}
	
	public static void main(String[] args) {			
			
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			String input = null,fileName = null;			
			int k = Integer.parseInt(args[0]);
			int n = Integer.parseInt(args[1]);
			Double V[] = new Double[n];
			
			System.out.print("TopK>");
			try{
				input = br.readLine();
			}
			catch(IOException e){
				System.out.println("Error caught while taking input from user");
				System.exit(0);
			}
			String temp[] = input.split(" ");
			if(temp==null || temp.length!=2 || !temp[0].equalsIgnoreCase("init")){				
				System.out.println("Invalid Input "+temp.length);
				System.exit(0);
			}
			fileName = temp[1];
			System.out.print("TopK>");
			try{
				input = br.readLine();
			}
			catch(IOException e){
				System.out.println("Error caught while taking input from user");
				System.exit(0);
			}
			temp = input.split(" ");
			if(temp==null || temp.length!=(n+1) ){
				System.out.println("Invalid Input "+temp.length);
				System.exit(0);
			}			
			
			for(int i=1;i<=n;i++){
				V[i-1]=Double.parseDouble(temp[i]);
			}
			
			TopK obj = new TopK(n,k,V);
			//INIT FUNCTION INVOKED
			obj.init(fileName);
			if(temp[0].equalsIgnoreCase("run1")){
				obj.printStack(obj.thresholdAlgo());
			}
			else if(temp[0].equalsIgnoreCase("run2")){ 
				obj.printStack(obj.naiveAlgo());
			}
			else{
				System.out.println("Invalid Input "+temp.length);
				System.exit(0);
			}
		}
}



public class MainClass {
	
	public static void main(String[] args){
		int[] arr={1,4,6,8,9,7,5,3,2};
		ResourceAllocator ra=new ResourceAllocator(arr);
		StopWatch st=new StopWatch();
		int[] arr1={2,3,4,1,1,2,3,2,2};
		ra.register(1, arr1);
		int[] arr2={1,3,1,1,1,2,3,4,2};
		ra.register(2,arr2);
		int[] arr3={1,3,1,1,2,2,3,0,2};
		ra.register(3,arr3);
		System.out.println(ra.getAvailable());
		int[] req1={1,0,0,1,0,2,3,0,2};
		ra.request(1, req1);
		System.out.println(ra.getAvailable());
		int[] req2={2,3,1,2,1,2,3,4,2};
		ra.request(2, req2);
		System.out.println(ra.getAvailable());
		ra.leave(2);
		int[] req3={1,0,1,1,2,0,3,0,2};
		ra.request(3, req3);
		System.out.println(ra.getAvailable());
		ra.deadlocked();
		System.out.println(ra.getAvailable());
		System.out.println(ra.processStatus(1));
		System.out.println(st.elapsedTime());
	}
	

}

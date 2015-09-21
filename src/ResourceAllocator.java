import java.lang.management.*;

class ResourceAllocator {

	public enum Status {
		notstarted, notblocked, blocked, killed
	};

	private final int MaxP = 100;
	private int[] queue = new int[MaxP]; // waiting queue of blocked processes;
	private int waiting = 0; // number of processes in the waiting "queue";

	private Status[] status = new Status[MaxP]; // status of all processes

	private int[] R = new int[9]; // max available resources (cf. Slide #236)
	private int[] V = new int[9]; // available resources (cf. Slide #236)
	private int[][] C = new int[MaxP][9]; // max claims (cf. Slide #236)
	private int[][] A = new int[MaxP][9]; // currently allocated (cf. Slide
											// #236)
	private int[] W = new int[MaxP]; // waiting for (cf. Slide #236)
	private int[] pArray;// array of processes
	private int elemCount=0;
	private Boolean[] deadlocked = new Boolean[MaxP];

	public ResourceAllocator(int[] R) {// constructor for the class
										// ResourceAllocator
		int i = 0;

		for (i = 0; i < 9; i++) {// sets all resources as available
			this.R[i] = R[i];
			V[i] = R[i];
		}

		for (i = 0; i < MaxP; i++) {
			status[i] = Status.notstarted; // give all the potential processes
											// status "notstarted"
			deadlocked[i] = false;
		}
		
	}

	public Status register(int pid, int[] max) { // register a new process
		if (pid <= MaxP) { // if the new process doesnt exceed the limit of max
							// processes running
			status[pid] = Status.notblocked; // assign status "notblocked" to
												// the current process
			pArray=new int[MaxP];	//add a new process to an array of processes
			pArray[elemCount++]=pid;
			for (int i = 0; i < 9; i++)
				C[pid-1][i] = max[i]; // add a process request to a request
										// matrix

		}
		return status[pid];
	};

	public void leave(int pid) { // release the resources when the process is
									// finished
		release(pid, A[pid-1]); // call a release function to release resources
								// used in the project and return them to
								// available
		for (int j = 0; j < 9; j++) {
			C[pid-1][j] = 0;
		}

	};

	public Status request(int pid, int[] req) {// request resources for a
												// process
		boolean flag = true;
		for (int j = 0; j < 9; j++)
			// add resources to an allocation matrix
			A[pid-1][j] = req[j];//
		//flag = isSafe();// run the function isSafe() to check if the allocation
						// of mentioned resources to a process wouldn't create a
						// threat of deadlocks
		//flag = noHoldAndWait(req);//checks if all resources are available
		if (flag == true) {
			status[pid] = Status.notblocked;// if the result is positive, assign
											// a status "unblocked" to a process
			for (int i = 0; i < 9; i++) { // decrease available resources
				
					V[i] = V[i] - A[pid-1][i];
			}
			System.out.println("Deadlock avoidance works:state is safe");
		} else {
			for (int j = 0; j < 9; j++)
				// delete resources from an allocation matrix
				A[pid-1][j] = 0;
			status[pid] = Status.blocked;// otherwise - block the process
			pushToWaiting(pid);
			System.out.println("Deadlock avoidance works:state is not safe");
		}
		return status[pid];
	};
	public boolean noHoldAndWait(int res[]){//deadlock prevention
		int counter=0;
		for(int i=0;i<9;i++)
			if(V[i]>=res[i])//check if there are enough available resources to satisfy the request
				counter++;
		if(counter==9)
			return true;
		else return false;//if not resources cant be granted, return false, meaning that the process cant be started
			
	}
	public void release(int pid, int[] rel) {// release resources that are not
												// longer used by a process
		int tmp[]=new int[9];
		tmp=rel;
		for (int i = 0; i < 9; i++) {
			V[i] = V[i] + tmp[i]; // add released resources to available
			A[pid-1][i] = A[pid-1][i] - tmp[i];// delete not used resources from an
											// allocation matrix
			
		}
	};

	public void pushToWaiting(int value) { // add a new process to a waiting
											// array
		if (waiting <MaxP) {
			W[waiting] = value;
			waiting++;
		}
	}

	public Boolean deadlocked() { // deadlock detection algorithm
		boolean deadlockDetected = false;
		int Work[] = new int[9]; // create array Work[] equal to V[]
		Work = V;
		boolean Finish[] = new boolean[MaxP];// create array Finish of a
												// dimension MaxP
		int numberOfRow = 0;
		numberOfRow = searchForZero(0);// get the first row number, which is not
										// equal to 0
		while (numberOfRow != 101 && numberOfRow < 100) {
			Finish[numberOfRow] = false;// set Finish[i], where i is an id of
										// a corresponding process, to 0
			if (compare(numberOfRow))
				for (int j = 0; j < 9; j++) {// update the Work[] and set Finish
												// value, corresponding to a
												// current process to true
					Work[j] = Work[j] + A[numberOfRow][j];
					Finish[numberOfRow] = true;
					numberOfRow = searchForZero(numberOfRow + 1);// find the
																	// next not
																	// null row
																	// in
																	// allocation
																	// matrix
				}
			else {

				deadlockDetected = true;// if the function compare(int) doesn't
										// return true, deadlock is found, the
										// loop will continue since our task is
										// to find all the processes that are
										// deadlocked
				release(numberOfRow+1, A[numberOfRow]);// release the resources
														// the process holds
				pushToWaiting(numberOfRow+1);// add a blocked process to a waiting
											// array
				status[numberOfRow] = Status.blocked;// assign to to a process
														// status "blocked"
				numberOfRow = searchForZero(numberOfRow + 1);//go to the next row to find all the deadlocked processes

			}
		}
		return deadlockDetected;
	}

	public boolean compare(int rowIndex) {// function, that compares the process
											// request with Work vector
		boolean flag = false;
		int Work[] = new int[9]; // create array Work[] equal to V[]
		Work = V;
		for (int j = 0; j < 9; j++)
			// search for a row of matrix C such, that C[i][j]<=Work[j]
			if (C[rowIndex][j] > Work[j]) {// if an element of a request matrix
											// C is bigger than the element of
											// matrix Work - terminate
				flag = false;
				break;
			} else
				flag = true;// else set the flag to true meaning that the
							// corresponding row of C matrix is less than Work[]

		return flag;
	}

	public int searchForZero(int startIndex) {// function that looks for a
												// process which has not null
												// row in an allocation matrix.
												// We pass the index of a
												// previous not null row, not to
												// repeat the loop
		int counter = 0;
		int rowIndex = 101;// indicating that there is no row with all 0
		for (int i = startIndex; i < MaxP; i++) {
			for (int j = 0; j < 9; j++) {
				if (A[i][j] == 0) // check the elements of allocation matrix
					counter++;// if the element is 0, increase the counter
			}
			if (counter < 9) {// if not all elements in the allocation matrix
								// are
								// 0
				rowIndex = i;// set rowIndex to a corresponding i value and
								// terminate
				break;
			}
			else
				counter=0;
		}
		return rowIndex;
	}

	public Status getStatus(int pid) {// returns status of a process
		return status[pid];
	}

	public String getStatusString(int pid) {
		switch (status[pid]) {
		case notstarted:
			return new String("Not started");
		case notblocked:
			return new String("Not blocked");
		case blocked:
			return new String("Blocked    ");
		case killed:
			return new String("Killed     ");
		}
		return new String("unknown status");
	}

	/*
	 * public int[] getGranted(int pid) {//grant resources to a process
	 * 
	 * }
	 * 
	 * public int[] getNotGranted(int pid) {//do not grant resources to a
	 * process // TBD }
	 */

	private Boolean isSafe() { // deadlock avoidance, ensures that the system
								// never enters unsafe state, that means no
								// deadlocks
		boolean safe = false;
		int counter = 0; // counts number of matching elements
		for (int i = 0; i < MaxP; i++) {
			for (int j = 0; j < 9; j++)
				if (C[i][j] - A[i][j] <= V[j]) // goes through C-A matrix and
												// looks for C-A raws, where all
												// elements are less then the
												// elements of V vector
					counter++; // increase the counter if the C-A element is
								// smaller then V element
			if (counter == 9)
				safe = true;
		}
		return safe;
	}

	public String processStatus(int pid) {
		String ret = "ID : " + pid + " ";
		int i = 0;

		ret += getStatusString(pid) + " Granted: ";

		for (i = 0; i < 9; i++)
			ret += A[pid-1][i] + " ";
		ret += " Waiting: ";
		for (i = 0; i < MaxP; i++)
			ret += W[i] + " ";
		// ret +="\n";

		return ret;
	}

	public String getAvailable() {
		String ret = "Available: ";
		int i = 0;
		for (i = 0; i < 9; i++)
			ret += V[i] + " ";
		return ret;
	}
}

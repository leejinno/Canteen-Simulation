// File: <Customer>
// Description: <Implement the stages in takeAction()>
// Project: <1>
//
// ID: <6588152>
// Name: <Jinnipa Leepong>
// Section: <2>
//
// On my honor, <Jinnipa Leepong>, this project assignment is my own work
// and I have not provided this code to any other students.


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Customer {
	
	//*********************** DO NOT MODIFY ****************************//
	public static enum CustomerType{DEFAULT, STUDENT, PROFESSOR, ATHLETE, ICTSTUDENT};	//Different types of customers 
	private static int customerRunningNumber = 1;	//static variable for assigning a unique ID to a customer
	private CanteenICT canteen = null;	//reference to the CanteenICT object
	private int customerID = -1;		//this customer's ID
	protected CustomerType customerType = CustomerType.DEFAULT;	//the type of this customer, initialized with a DEFAULT customer.
	protected List<FoodStall.Menu> requiredDishes = new ArrayList<FoodStall.Menu> ();	//List of required dishes
	
	public static enum Payment{DEFAULT, CASH, MOBILE};
	public static final int[] PAYMENT_TIME = {3, 2, 1};
	protected Payment payment = Payment.DEFAULT;
	
	protected int state = 0; // 0 wait-to-enter, 1 wait-to-order, 2 ordering, 
							 // 3 making payment, 4 wait-to-seat, 5 siting, 
							 // 6 eating, 7 done
	//*****************************************************************//
	
	
	/**
	 * Constructor. Initialize canteen reference, default customer type, and default payment method. 
	 * 				Initialize other values as needed
	 * @param _canteen
	 */
	public Customer(CanteenICT _canteen)
	{
		//******************* YOUR CODE HERE **********************
		this.canteen = _canteen;
		this.customerID = customerRunningNumber;
		customerRunningNumber++;
		this.setRequiredDishes();
		//*****************************************************
	}
	
	/**
	 * Constructor. Initialize canteen reference, default customer type, and specific payment method.
	 * 				Initialize other values as needed 
	 * @param _canteen
	 * @param payment
	 */
			
	public Customer(CanteenICT _canteen, Payment payment)	
	{
		//******************* YOUR CODE HERE **********************
		this.canteen = _canteen;
		this.payment = payment;
		this.customerID = customerRunningNumber;
		customerRunningNumber++;
		this.setRequiredDishes();
		//*****************************************************
	}
	
	/**
	 * Depends on the current state of the customer, different action will be taken
	 * @return true if the customer has to move to the next queue, otherwise return false
	 */
	public boolean takeAction()
	{
		//************************** YOUR CODE HERE **********************//
		FoodStall thisStall = new FoodStall(null, canteen, requiredDishes);
		for(FoodStall check : this.canteen.getFoodStalls()) {
			if(check.getCustomerQueue().contains(this)) {
				thisStall = check;
				break;
			}
		}
		
		Table thisTable = new Table();
		for(Table check1 : this.canteen.getTable()) {
			if(check1.getSeatedCustomers().contains(this)) {
				thisTable = check1;
				break;
			}
		}
		
		if(this.state == 0) {
			this.state++;
		}
		
		if(this.state == 1) {
			
			List<FoodStall> list = this.canteen.getFoodStalls();
			FoodStall AvailableStall = list.get(0);
			for(FoodStall stall : list) {	
					if( AvailableStall.getCustomerQueue().size() > stall.getCustomerQueue().size()) {
						if(stall.getMenu().containsAll(this.getRequiredFood())) {
							AvailableStall = stall;
						}
					}
			}
			
			if( AvailableStall.getCustomerQueue().size() < FoodStall.MAX_QUEUE && this == this.canteen.getWaitToEnterQueue().get(0)){		
				jot("@"+ this.getCode()+ "-"+ this.state + " queues up at " + AvailableStall.getName()+ ", and waiting to order.");
				AvailableStall.getCustomerQueue().add(this);
				this.canteen.getWaitToEnterQueue().set(0, new Customer());
				this.state++;
				return true;
			}
		}
		
		if(this.state == 2) {
			
			if(thisStall.getCustomerQueue().size()!= 0) {
				//thisStall.isWaitingForOrder() &&
				if( this==thisStall.getCustomerQueue().get(0) )  {
					int time = thisStall.takeOrder(requiredDishes);
					if(time != -1) {
						jot("@"+ this.getCode()+ "-"+ this.state + " orders from " + thisStall.getName() +", and will need to wait for " + time + " periods to cook.");
					this.state++;
					return false;
					}				
				}				
			}
		}
		
		if(this.state == 3) {
			
			if(thisStall.isReadyToServe()) {
				thisStall.takePayment(payment);
				jot("@"+ this.getCode()+ "-"+ this.state + " pays at " + thisStall.getName()+ " using "+this.getPayment()+" payment which requires " + thisStall.takePaymentTime + " period(s) to process payment.");
				this.state++;
				return false;
			}
		}
		
		if(this.state == 4) {
		
			if(thisStall.isPaid()) {
				thisStall.serve();
				jot("@"+ this.getCode()+ "-"+ this.state + " retrives food from " + thisStall.getName() + ", and goes to Waiting-to-Seat Queue.");
				
				if(this ==  thisStall.getCustomerQueue().get(0)) {
					this.canteen.getWaitToSeatQueue().add(this);
					thisStall.getCustomerQueue().set(0, new Customer());
					this.state++;
					return true;
				}	
			}
		}
		
		if(this.state == 5) {
			
			List<Table> list = this.canteen.getTable();
			Table AvailableTable = list.get(0);	
			for(Table table : list){				
					if( AvailableTable.getSeatedCustomers().size() > table.getSeatedCustomers().size()) {
						AvailableTable = table;
					}
			}		
			if( !AvailableTable.isFull() && this == this.canteen.getWaitToSeatQueue().get(0)){				
				jot("@"+ this.getCode()+ "-"+ this.state+ " sits at Table " + AvailableTable.getID()+".");				
				AvailableTable.getSeatedCustomers().add(this);				
				this.canteen.getWaitToSeatQueue().set(0, new Customer());				
				this.state++;
				return true;
			}	
		}			
		
		if(this.state == 6) {
			
			if(this.eatingTime == -1) {
				jot("@"+ this.getCode()+ "-"+ this.state + " eats at the table, and will need " + this.eatingTime + " periods to eat his/her meal.");
				int total = 0;
				for(FoodStall.Menu x : requiredDishes) {
					total += FoodStall.EAT_TIME[x.ordinal()];
				}
				this.eatingTime = total;
			}
			
			if( this.eatingTime > 0 ) {
				this.eatingTime --;
			}else{
				this.state++;
			}
		}
			
		if(this.state == 7) {
			jot("@"+ this.getCode()+ "-"+ this.state +" is done eating.");
			this.canteen.getDoneQueue().add(this);
			thisTable.getSeatedCustomers().set(0, new Customer());	
			this.state ++;		
		}

		
		

		return false;
		//**************************************************************//
		
	}
	
	
	//******************************************** YOUR ADDITIONAL CODE HERE (IF ANY) *******************************//
	
	protected int eatingTime = -1;
	protected boolean imposter = false;
	/**
	 * fake
	 */
	public Customer() {
		this.imposter = true;
		this.customerID = 666;
		this.state = -999;
		this.canteen = new CanteenICT(null);
	}
	
	public boolean isSus() {
		return this.imposter;
	}
	
	
	/**
	 * to set required dish based on customer type
	 *  declare this in the constructor
	 */
	public void setRequiredDishes( ){
		switch(customerType) {
			case DEFAULT: 
				this.requiredDishes = Arrays.asList(FoodStall.Menu.NOODLES, FoodStall.Menu.DESSERT, FoodStall.Menu.MEAT, FoodStall.Menu.SALAD, FoodStall.Menu.BEVERAGE);
				break;
			case STUDENT:
				this.requiredDishes = Arrays.asList(FoodStall.Menu.DESSERT, FoodStall.Menu.DESSERT, FoodStall.Menu.DESSERT, FoodStall.Menu.DESSERT, FoodStall.Menu.DESSERT);
				break;
			case PROFESSOR:
				this.requiredDishes = Arrays.asList(FoodStall.Menu.NOODLES, FoodStall.Menu.BEVERAGE);
				break;
			case ATHLETE:
				this.requiredDishes = Arrays.asList(FoodStall.Menu.MEAT, FoodStall.Menu.MEAT, FoodStall.Menu.MEAT, FoodStall.Menu.SALAD, FoodStall.Menu.BEVERAGE);
				break;
			case ICTSTUDENT:
				break;
			default:
				break;
		}
	}
	
	
	//****************************************************************************************************//
				
	

	//***************For hashing, equality checking, and general purposes. DO NOT MODIFY **************************//	
	
	public CustomerType getCustomerType()
	{
		return this.customerType;
	}
	
	public int getCustomerID()
	{
		return this.customerID;
	}
	
	public Payment getPayment()
	{
		return this.payment;
	}
	
	public List<FoodStall.Menu> getRequiredFood()
	{
		return this.requiredDishes;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + customerID;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Customer other = (Customer) obj;
		if (customerID != other.customerID)
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return "Customer [customerID=" + customerID + ", customerType=" + customerType +", payment="+payment.name()+"]";
	}

	public String getCode()
	{
		return this.customerType.toString().charAt(0)+""+this.customerID;
	}
	
	/**
	 * print something out if VERBOSE is true 
	 * @param str
	 */
	public void jot(String str)
	{
		if(CanteenICT.VERBOSE) System.out.println(str);
		
		if(CanteenICT.WRITELOG) CanteenICT.append(str, canteen.name+"_state.log");
	}


	//*************************************************************************************************//
	
}

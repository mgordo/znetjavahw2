package market;



@SuppressWarnings("serial")
public class EmptyInventoryException extends Exception {

	public String message;

    public EmptyInventoryException(String message){
        this.message = message;
    }

    // Overrides Exception's getMessage()
    @Override
    public String getMessage(){
        return message;
    }

}

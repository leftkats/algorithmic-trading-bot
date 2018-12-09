package left.Model;

import java.util.ArrayList;
import java.util.List;

public class Models {

	public List<Model> models = new ArrayList<Model>();

	private static Models instance = new Models();

	public static Models getInstance()
	{
		return instance;
	}

	public void init()
	{
		BookArchiver ba = new BookArchiver();
		TradesArchiver ta = new TradesArchiver();
		Kreg kreg = new Kreg();
		Thread th = new Thread( kreg );
		th.start();
		models.add(kreg);
	}

	public String getKregPosition() {
		return models.get(2).getPositions();
	}

}

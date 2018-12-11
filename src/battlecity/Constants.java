package battlecity;


public interface Constants {
// Game stages
	public static final int GAME_START = 0;
	public static final int IN_PROGRESS = 1;
	public final int GAME_END = 2;
	public final int WAITING_FOR_PLAYERS = 3;
	public static final int LOADING = 4;
	
	public static final int PORT = 999;
//	localhost will be used for now - replace later
	public static final String IPADD = "localhost";
	
//	Movements
    public final int UP = 1;
    public final int DOWN = 2;
    public final int LEFT = 3;
    public final int RIGHT = 4;
}
package ovh.stranck.javaTimecode;

public class TimecodePlayer {
	private Timecode t;
	private boolean playing = true;
	private double speed;
	private int offset;
	private int zFrame;
	private long zMs;

	public TimecodePlayer(Timecode t){
		this(t, 0);
	}
	public TimecodePlayer(Timecode t, double speed){
		this.t = t;
		setSpeed(speed);
	}

	private int convertToFrames(int value){
		return (int) (value * speed * t.getFramerate().getIntegerFramerate() / 1000 + zFrame + offset);
	}

	public synchronized void updateTimecodeTime(int msOffset){
		if(playing){
			//System.out.println((System.currentTimeMillis() - zMs) + " ");
			t.setTimeWithoutUpdatingStartPoint(convertToFrames((int) (System.currentTimeMillis() - zMs + msOffset)));
		}
    }

	public synchronized void updateStartPoint(){
		zFrame = t.getTotalFrames();
		zMs = System.currentTimeMillis();
	}

	public synchronized TimecodePlayer play(){
		updateStartPoint();
		playing = true;
		return this;
	}
	public synchronized TimecodePlayer pause(){
		playing = false;
		return this;
	}

	public synchronized TimecodePlayer setSpeed(double speed){
		this.speed = speed;
		updateStartPoint();
		return this;
	}
	public double getSpeed(){
		return speed;
	}
	public TimecodePlayer setOffset(int offset){
		this.offset = offset;
		return this;
	}
	public int getOffset(){
		return offset;
	}

	public Timecode getTimecode(){
		return t;
	}
}

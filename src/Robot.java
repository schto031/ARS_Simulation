// Written by Tom
// ignore this class

public class Robot {
	
	public int posX;
	public int posY;
	
	public double posXD;
	public double posYD;
	
	public double Vr; //velocity right wheel
	public double Vl; //velocity left wheel
	
	public double l; //length of axis of robot
	public double rotation;
	public double R;
	public double ICCx; //double value casting the double icc position to an integer because of pixels
	public double ICCy;
	public double ICCxd; //double value representing the real x position of icc
	public double ICCyd; //double value representing the real y position of icc
	public double teta; //angle of robot with Xaxis
	
	
	public Robot(int posX, int posY) {
		this.posX = posX;
		this.posY = posY;
		this.posXD = this.posX;
		this.posYD = this.posY;
		this.Vr = 0;
		this.Vl = 0;
		this.l = 100; //simulation does not start working until you hit velocity of 100, this has something to do with l
		this.teta = 0;
		calculateParams();
		calculateICC();
	}
	
	public void calculateICC() {
		this.ICCxd = (this.posX - this.R*Math.sin(this.teta));
		this.ICCyd = (this.posY + this.R*Math.cos(this.teta));
		System.out.println("New ICC: "+ICCxd+" "+ICCyd);
	}
	
	public void calculateParams() {
		if(Vr == Vl) {
			R=Integer.MAX_VALUE;
			rotation = 0;
		} else if(Vr == -Vl) {
			R = 0;
			rotation = (Vr-Vl)/l;
		}else {
			rotation = (Vr-Vl)/l;
			R= (l/2)*((Vl+Vr)/(Vr-Vl));
		}
	}
	
	public void incVelRightWheel(int x) {
		this.Vr += x;
		calculateParams();
		calculateICC();
	}
	
	public void incVelLeftWheel(int x) {
		this.Vl += x;
		calculateParams();
		calculateICC();
	}
	
	public void stop() {
		this.Vl=0;
		this.Vr=0;
		calculateParams();
		calculateICC();
	}
	
	public void update(double time) {
		//System.out.println("Vl="+this.Vl+" Vr="+this.Vr);
		var V=(this.Vl+this.Vr)/2;
		this.posXD = ((Math.cos(rotation*time)))*(this.posXD-this.ICCxd)+
					((-Math.sin(rotation*time)))*(this.posYD-this.ICCyd)+
					V*time+
					this.ICCxd;
		this.posYD = ((Math.sin(rotation*time)))*(this.posXD-this.ICCxd)+
					((Math.cos(rotation*time)))*(this.posYD-this.ICCyd)+
					V*time+
					this.ICCyd;
		this.teta = this.teta + rotation*time;
		this.posX= (int)posXD;
		this.posY= (int)posYD;
		this.ICCx = (int) ICCxd;
		this.ICCy = (int) ICCyd;
		System.out.println("New pos: X="+posX+" Y="+posY+" teta="+teta+", Velocities Vl:"+Vl+", Vr:"+Vr);
		calculateParams();
		calculateICC();
	}
	
	public int coordToNumber(int x, int y) {
		return ((y+10)*640)+x;
	}
}

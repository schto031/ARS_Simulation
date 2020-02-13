public class Robot {
	
	public int posX;
	public int posY;
	
	public double posXD;
	public double posYD;
	
	public int Vr; //velocity right wheel
	public int Vl; //velocity left wheel
	
	public int l; //length of axis of robot
	public double rotation;
	public int R;
	public int ICCx;
	public int ICCy;
	public double teta; //angle of robot wit Xaxis
	
	
	public Robot(int posX, int posY) {
		this.posX = posX;
		this.posY = posY;
		this.posXD = this.posX;
		this.posYD = this.posY;
		this.Vr = 0;
		this.Vl = 0;
		this.l = 50;
		this.teta = 0;
		calculateParams();
		calculateICC();
	}
	
	public void calculateICC() {
		this.ICCx = (int) Math.round(this.posX - this.R*Math.sin(this.teta));
		this.ICCy = (int) Math.round(this.posY + this.R*Math.cos(this.teta));
		System.out.println("New ICC: "+ICCx+" "+ICCy);
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
		this.posXD = ((Math.cos(rotation*time)))*(this.posXD-this.ICCx)+
					((-Math.sin(rotation*time)))*(this.posYD-this.ICCy)+
					this.ICCx;
		this.posYD = ((Math.sin(rotation*time)))*(this.posXD-this.ICCx)+
					((Math.cos(rotation*time)))*(this.posYD-this.ICCy)+
					this.ICCy;
		this.teta = this.teta + rotation*time;
		this.posX= (int)posXD;
		this.posY= (int)posYD;
		System.out.println("New pos: X="+posX+" Y="+posY+" teta="+teta+", Velocities Vl:"+Vl+", Vr:"+Vr);
		calculateParams();
		calculateICC();
	}
	
	public int coordToNumber(int x, int y) {
		return ((y+10)*640)+x;
	}
	

}

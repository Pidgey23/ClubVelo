package be.pasque.Model;

public class Inscription {
    private Long id;
    private boolean isDriver = false;
    private boolean isPassenger = false;
    private boolean needsBikeTransport = false;

    private Ride ride;
    private Member member;
    private Vehicle vehicleUsed;  

    public Inscription() {}

    public Inscription(Ride ride, Member member, boolean isDriver, boolean isPassenger, boolean needsBikeTransport) {
        this.ride = ride;
        this.member = member;
        this.isDriver = isDriver;
        this.isPassenger = isPassenger;
        this.needsBikeTransport = needsBikeTransport;
    }


    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public boolean isDriver() { return isDriver; }
    public void setDriver(boolean driver) { isDriver = driver; }

    public boolean isPassenger() { return isPassenger; }
    public void setPassenger(boolean passenger) { isPassenger = passenger; }

    public boolean isNeedsBikeTransport() { return needsBikeTransport; }
    public void setNeedsBikeTransport(boolean needsBikeTransport) { this.needsBikeTransport = needsBikeTransport; }

    public Ride getRide() { return ride; }
    public void setRide(Ride ride) { this.ride = ride; }

    public Member getMember() { return member; }
    public void setMember(Member member) { this.member = member; }

    public Vehicle getVehicleUsed() { return vehicleUsed; }
    public void setVehicleUsed(Vehicle vehicleUsed) { this.vehicleUsed = vehicleUsed; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(member.toString());
        if (isDriver) sb.append(" (Conducteur");
        if (isPassenger) sb.append(isDriver ? " + Passager" : " (Passager");
        if (needsBikeTransport) sb.append(" + Vélo");
        if (isDriver || isPassenger) sb.append(")");
        return sb.toString();
    }
}
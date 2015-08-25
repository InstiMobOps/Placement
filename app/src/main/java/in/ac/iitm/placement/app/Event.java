package in.ac.iitm.placement.app;

import java.util.Comparator;
import java.util.Date;

/**
 * Created by arun on 17-Jul-15.
 */
public class Event {
    String event, date, name, venue, discription, time;
    Date FormatedDate;
    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getDate() {
        return date;
    }

    public Date getFormatedDate() {
        return FormatedDate;
    }

    public void setFormatedDate(Date formatedDate) {
        this.FormatedDate = formatedDate;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVenue() {
        return venue;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    public String getDiscription() {
        return discription;
    }

    public void setDiscription(String discription) {
        this.discription = discription;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
    public static Comparator<Event> NameComparator = new Comparator<Event>() {

        public int compare(Event s1, Event s2) {
            String Name1 = s1.getName().toUpperCase();
            String Name2 = s2.getName().toUpperCase();
            String na=Name2;
            //ascending order
            return Name1.compareTo(Name2);
            //descending order
            //return StudentName2.compareTo(StudentName1);
        }};
    public static Comparator<Event> DateComparator = new Comparator<Event>() {

        public int compare(Event s1, Event s2) {

            return s1.getFormatedDate().compareTo(s2.getFormatedDate());
              //descending order
            //return StudentName2.compareTo(StudentName1);
        }};
}

package com.driver.services;

import com.driver.EntryDto.AddTrainEntryDto;
import com.driver.EntryDto.SeatAvailabilityEntryDto;
import com.driver.model.Passenger;
import com.driver.model.Station;
import com.driver.model.Ticket;
import com.driver.model.Train;
import com.driver.repository.TrainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class TrainService {

    @Autowired
    TrainRepository trainRepository;

    public Integer addTrain(AddTrainEntryDto trainEntryDto){

        //Add the train to the trainRepository
        //and route String logic to be taken from the Problem statement.
        //Save the train and return the trainId that is generated from the database.
        //Avoid using the lombok library
        List<Station> StationList=trainEntryDto.getStationRoute();
        StringBuilder stringBuilder=new StringBuilder();
        for(Station station:StationList){
            stringBuilder.append(station).append(",");
        }
        if(stringBuilder.length()>0) {
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        }
        String trainRoute=stringBuilder.toString();

        Train train=new Train();
        train.setRoute(trainRoute);
        train.setDepartureTime(trainEntryDto.getDepartureTime());
        train.setNoOfSeats(trainEntryDto.getNoOfSeats());

        train=trainRepository.save(train);
        return train.getTrainId();
    }

    public Integer  calculateAvailableSeats(SeatAvailabilityEntryDto seatAvailabilityEntryDto){

        //Calculate the total seats available
        //Suppose the route is A B C D
        //And there are 2 seats avaialble in total in the train
        //and 2 tickets are booked from A to C and B to D.
        //The seat is available only between A to C and A to B. If a seat is empty between 2 station it will be counted to our final ans
        //even if that seat is booked post the destStation or before the boardingStation
        //Inshort : a train has totalNo of seats and there are tickets from and to different locations
        //We need to find out the available seats between the given 2 stations.
          int trainId=seatAvailabilityEntryDto.getTrainId();
          Optional<Train> trainOptional=trainRepository.findById(trainId);
          int availableSeats=0;


          if(trainOptional.isPresent()){
              Train train=trainOptional.get();
              // check if the train is going through the route that the user specified
              String TrainRoute= train.getRoute();


              List<String> RouteList=Arrays.asList(TrainRoute.split(","));
               int startPoint= RouteList.indexOf(seatAvailabilityEntryDto.getFromStation().name());
               int endPoint= RouteList.indexOf(seatAvailabilityEntryDto.getToStation().name());

               // if the route has  the departure and destination in route map, then,
              // Can see for the seats availability
              if(startPoint>=0&& endPoint>0&& startPoint<endPoint){
                   // Means the route departure and destination defined by user, is in the train route and
                  // Trains has number of tickets booked, check each and every ticket and find the number of passenger
                  // will be present in that two points and subtract from the total number of seats
                  // to see the available seats
                  int totalSeats= train.getNoOfSeats();
                  int passengersPresent=0;


                  for(Ticket ticket:train.getBookedTickets()){
                       int ticketFrom=RouteList.indexOf(ticket.getFromStation().name());
                       int ticketTo=RouteList.indexOf(ticket.getToStation().name());

                       //  check if this ticket passengers will be there for the start and end point
                      if(ticketFrom<endPoint && ticketTo>startPoint){
                          // person checking this availablility for boarding in the starpoint index place,
                          // so in that place, the particular ticket passengers are  present in train
                          passengersPresent+=ticket.getPassengersList().size();
                      }

                  }
                      availableSeats=totalSeats-passengersPresent;

              }

          }
       return availableSeats;
    }

    public Integer calculatePeopleBoardingAtAStation(Integer trainId,Station station) throws Exception{
        int CountOfPassengers=0;

        //We need to find out the number of people who will be boarding a train from a particular station
        //if the trainId is not passing through that station
        //throw new Exception("Train is not passing from this station");
        //  in a happy case we need to find out the number of such people.

        // find the train detaisl from the db through trainId;
        Optional<Train> optionalTrain=trainRepository.findById(trainId);
        if(optionalTrain.isPresent()){
            Train train=optionalTrain.get();

            // trains has its tickets , go through each and every ticket  and see the boarding point of ticket
//             and count the passengers count for ticket
               List<String> RouteList=Arrays.asList(train.getRoute().split(","));
               if(RouteList.contains(station)){

                   for(Ticket ticket: train.getBookedTickets()){
                       if(ticket.getFromStation()==station){
                           CountOfPassengers+=ticket.getPassengersList().size();
                       }
                   }

               }else{
                   throw new Exception("Train is not passing from this station");

               }
        }

        return CountOfPassengers;
    }

    public Integer calculateOldestPersonTravelling(Integer trainId){
        int OldestAge=0;

        //Throughout the journey of the train between any 2 stations
        //We need to find out the age of the oldest person that is travelling the train
        //If there are no people travelling in that train you can return 0

        Optional<Train> optionalTrain=trainRepository.findById(trainId);
        if(optionalTrain.isPresent()){
            Train train= optionalTrain.get();
            // train has tickets and each tickets has some passengers, go through every passengers and
            // check the age

            for(Ticket ticket:train.getBookedTickets()){
                for(Passenger passenger: ticket.getPassengersList()){
                    if(passenger.getAge()>OldestAge) OldestAge=passenger.getAge();
                }
            }
        }

        return OldestAge;
    }

    public List<Integer> trainsBetweenAGivenTime(Station station, LocalTime startTime, LocalTime endTime){
            List<Integer> trainNoList=new ArrayList<>();
        //When you are at a particular station you need to find out the number of trains that will pass through a given station
        //between a particular time frame both start time and end time included.
        //You can assume that the date change doesn't need to be done ie the travel will certainly happen with the same date (More details
        //in problem statement)
        //You can also assume the seconds and milli seconds value will be 0 in a LocalTime format.

        // Go through the trainslist  and check if train is going through particular station mentioned
        // if so, then check the station point and  calculate the time it will reach by that station
        // if trains reaching the station with the time period specified, then count into list
        List<Train> trainList=trainRepository.findAll();
        int StartTime=startTime.getHour()*60 + startTime.getMinute();
        int EndTime=endTime.getHour()*60+endTime.getMinute();
        if(!(StartTime<=EndTime))return trainNoList;


         for(Train train: trainList){
             List<String> RouteList=Arrays.asList(train.getRoute().split(","));
             if(RouteList.contains(station.name())){
                 int idxOfStation=RouteList.indexOf(station.name());
                 int StartTimeOfTrain=train.getDepartureTime().getHour()*60 +train.getDepartureTime().getMinute();
                 int timeAtStation=StartTimeOfTrain +(idxOfStation*60);

                 // check if the time of the train at the station lies between the waiting period
                  if(timeAtStation>=StartTime && timeAtStation<=EndTime){
                       trainNoList.add(train.getTrainId());
                  }
             }
         }


        return trainNoList;
    }

}

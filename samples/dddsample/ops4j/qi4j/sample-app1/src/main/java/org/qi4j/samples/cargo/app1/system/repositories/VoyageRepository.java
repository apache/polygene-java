package org.qi4j.samples.cargo.app1.system.repositories;

import org.qi4j.samples.cargo.app1.model.location.Location;
import org.qi4j.samples.cargo.app1.model.voyage.CarrierMovement;
import org.qi4j.samples.cargo.app1.model.voyage.Schedule;
import org.qi4j.samples.cargo.app1.model.voyage.Voyage;
import org.qi4j.samples.cargo.app1.model.voyage.VoyageNumber;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.usecase.Usecase;
import org.qi4j.api.usecase.UsecaseBuilder;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;


/**
 *
 */
@Mixins(VoyageRepository.VoyageRepositoryMixin.class)
public interface VoyageRepository extends ServiceComposite {

    Voyage findVoyageByVoyageNumber(VoyageNumber voyageNumber);

    Voyage findVoyageByVoyageIdentity(String voyageIdentity);

    public static abstract class VoyageRepositoryMixin
            implements VoyageRepository, Activatable {
        @Structure
        private UnitOfWorkFactory uowf;

        @Structure
        private ValueBuilderFactory vbf;

        @Service
        private LocationRepository locationRepository;

        public Voyage findVoyageByVoyageNumber(final VoyageNumber voyageNumber) {
            return findVoyageByVoyageIdentity(voyageNumber.number().get());
        }

        public Voyage findVoyageByVoyageIdentity(final String voyageIdentity) {
            UnitOfWork uow = uowf.currentUnitOfWork();
            return uow.get(Voyage.class, Builder.createVoyageIdentity( voyageIdentity));
        }

        /* Initializes the database first time. */

        public void activate() throws Exception {
            Usecase usecase = UsecaseBuilder.newUsecase("Populate Sample Voyages");
            UnitOfWork uow = uowf.newUnitOfWork(usecase);
            try {

                final Location HONGKONG = locationRepository.findLocationByUnLocode("CNHKG");
                final Location TOKYO = locationRepository.findLocationByUnLocode("JNTKO");
                final Location NEWYORK = locationRepository.findLocationByUnLocode("USNYC");
                final Location CHICAGO = locationRepository.findLocationByUnLocode("USCHI");
                final Location STOCKHOLM = locationRepository.findLocationByUnLocode("SESTO");
                final Location ROTTERDAM = locationRepository.findLocationByUnLocode("NLRTM");
                final Location MELBOURNE = locationRepository.findLocationByUnLocode("AUMEL");
                final Location HAMBURG = locationRepository.findLocationByUnLocode("DEHAM");
                final Location HELSINKI = locationRepository.findLocationByUnLocode("FIHEL");
                final Location DALLAS = locationRepository.findLocationByUnLocode("USDAL");
                final Location HANGZOU = locationRepository.findLocationByUnLocode("CNHGH");
                final Location SHANGHAI = locationRepository.findLocationByUnLocode("CNSHA");

                Voyage v100 = new VoyageRepository.Builder(vbf, uow, "V100", HONGKONG).
                        addMovement(TOKYO, toDate("2009-03-03"), toDate("2009-03-05")).
                        addMovement(NEWYORK, toDate("2009-03-06"), toDate("2009-03-09")).
                        build();
                Voyage v200 = new VoyageRepository.Builder(vbf, uow, "V200", TOKYO).
                        addMovement(NEWYORK, toDate("2009-03-06"), toDate("2009-03-08")).
                        addMovement(CHICAGO, toDate("2009-03-10"), toDate("2009-03-14")).
                        addMovement(STOCKHOLM, toDate("2009-03-14"), toDate("2009-03-16")).
                        build();
                Voyage v300 = new VoyageRepository.Builder(vbf, uow, "V300", TOKYO).
                        addMovement(ROTTERDAM, toDate("2009-03-22"), toDate("2009-03-25")).
                        addMovement(HAMBURG, toDate("2009-03-25"), toDate("2009-03-26")).
                        addMovement(MELBOURNE, toDate("2009-03-28"), toDate("2009-04-03")).
                        addMovement(TOKYO, toDate("2009-04-03"), toDate("2009-04-06")).
                        build();
                Voyage v400 = new VoyageRepository.Builder(vbf, uow, "V400", HAMBURG).
                        addMovement(STOCKHOLM, toDate("2009-03-14"), toDate("2009-03-15")).
                        addMovement(HELSINKI, toDate("2009-03-15"), toDate("2009-03-16")).
                        addMovement(HAMBURG, toDate("2009-03-20"), toDate("2009-03-22")).
                        build();


                /*
                 * Voyage number 0100S (by ship)
                 * <p/>
                 * Hongkong - Hangzou - Tokyo - Melbourne - New York
                 */
                Voyage HONGKONG_TO_NEW_YORK =
                        new VoyageRepository.Builder(vbf, uow, "0100S", HONGKONG).
                                addMovement(HANGZOU, toDate("2008-10-01", "12:00"), toDate("2008-10-03", "14:30")).
                                addMovement(TOKYO, toDate("2008-10-03", "21:00"), toDate("2008-10-06", "06:15")).
                                addMovement(MELBOURNE, toDate("2008-10-06", "11:00"), toDate("2008-10-12", "11:30")).
                                addMovement(NEWYORK, toDate("2008-10-14", "12:00"), toDate("2008-10-23", "23:10")).
                                build();


                /*
                 * Voyage number 0200T (by train)
                 * <p/>
                 * New York - Chicago - Dallas
                 */
                Voyage NEW_YORK_TO_DALLAS =
                        new VoyageRepository.Builder(vbf, uow, "0200T", NEWYORK).
                                addMovement(CHICAGO, toDate("2008-10-24", "07:00"), toDate("2008-10-24", "17:45")).
                                addMovement(DALLAS, toDate("2008-10-24", "21:25"), toDate("2008-10-25", "19:30")).
                                build();

                /*
                 * Voyage number 0300A (by airplane)
                 * <p/>
                 * Dallas - Hamburg - Stockholm - Helsinki
                 */
                Voyage DALLAS_TO_HELSINKI =
                        new VoyageRepository.Builder(vbf, uow, "0300A", DALLAS).
                                addMovement(HAMBURG, toDate("2008-10-29", "03:30"), toDate("2008-10-31", "14:00")).
                                addMovement(STOCKHOLM, toDate("2008-11-01", "15:20"), toDate("2008-11-01", "18:40")).
                                addMovement(HELSINKI, toDate("2008-11-02", "09:00"), toDate("2008-11-02", "11:15")).
                                build();

                /*
                 * Voyage number 0301S (by ship)
                 * <p/>
                 * Dallas - Hamburg - Stockholm - Helsinki, alternate route
                 */
                Voyage DALLAS_TO_HELSINKI_ALT =
                        new VoyageRepository.Builder(vbf, uow, "0301S", DALLAS).
                                addMovement(HELSINKI, toDate("2008-10-29", "03:30"), toDate("2008-11-05", "15:45")).
                                build();

                /*
                 * Voyage number 0400S (by ship)
                 * <p/>
                 * Helsinki - Rotterdam - Shanghai - Hongkong
                 */
                Voyage HELSINKI_TO_HONGKONG =
                        new VoyageRepository.Builder(vbf, uow, "0400S", HELSINKI).
                                addMovement(ROTTERDAM, toDate("2008-11-04", "05:50"), toDate("2008-11-06", "14:10")).
                                addMovement(SHANGHAI, toDate("2008-11-10", "21:45"), toDate("2008-11-22", "16:40")).
                                addMovement(HONGKONG, toDate("2008-11-24", "07:00"), toDate("2008-11-28", "13:37")).
                                build();
                uow.complete();
            } catch (RuntimeException e) {
                uow.discard();
                throw e;
            }
        }

        public void passivate() throws Exception {
        }

        /**
         * @param date date string as yyyy-MM-dd
         * @return Date representation
         */
        private Date toDate(final String date) {
            return toDate(date, "00:00.00.000");
        }

        /**
         * @param date date string as yyyy-MM-dd
         * @param time time string as HH:mm
         * @return Date representation
         */
        private Date toDate(final String date, final String time) {
            try {
                return new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(date + " " + time);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }

    }

    /**
     * Builder pattern is used for incremental construction
     * of a Voyage aggregate. This serves as an aggregate factory.
     */
    public static final class Builder {

        private final List<CarrierMovement> carrierMovements = new ArrayList<CarrierMovement>();
        private final ValueBuilderFactory factory;
        private final UnitOfWork uow;
        private final String voyageNumberString;
        private Location departureLocation;

        public Builder(ValueBuilderFactory factory, UnitOfWork uow, String voyageNumber, Location departureLocation) {
            this.factory = factory;
            this.uow = uow;
            this.voyageNumberString = voyageNumber;
            this.departureLocation = departureLocation;
        }

        public Builder addMovement(Location arrivalLocation, Date departureTime, Date arrivalTime) {

            final ValueBuilder<CarrierMovement> builder = factory.newValueBuilder(CarrierMovement.class);
            CarrierMovement prototype = builder.prototype();
            prototype.departureLocationUnLocode().set(departureLocation.identity().get());
            prototype.arrivalLocationUnLocode().set(arrivalLocation.identity().get());
            prototype.departureTime().set(departureTime);
            prototype.arrivalTime().set(arrivalTime);
            carrierMovements.add(builder.newInstance());
            // Next departure location is the same as this arrival location
            this.departureLocation = arrivalLocation;
            return this;
        }

        public Voyage build() {
            final ValueBuilder<Schedule> builder = factory.newValueBuilder(Schedule.class);
            builder.prototype().carrierMovements().set(carrierMovements);
            final Schedule schedule = builder.newInstance();
            final EntityBuilder<Voyage> entityBuilder = uow.newEntityBuilder(Voyage.class, createVoyageIdentity(voyageNumberString));
            Voyage voyage = entityBuilder.instance();
            VoyageNumber voyageNumber = createVoyageNumber(voyageNumberString);
            voyage.voyageNumber().set(voyageNumber);
            voyage.schedule().set(schedule);
            return entityBuilder.newInstance();
        }

        private VoyageNumber createVoyageNumber(final String voyageNumberString) {
            ValueBuilder<VoyageNumber> builder = factory.newValueBuilder(VoyageNumber.class);
            builder.prototype().number().set(voyageNumberString);
            return builder.newInstance();
        }

        private static String createVoyageIdentity(final String voyageNumberString) {
            return "VoyageID:" + voyageNumberString;
        }

    }

}
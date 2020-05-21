package tutorial;

import org.powerbot.script.Condition;
import org.powerbot.script.Filter;
import org.powerbot.script.PollingScript;
import org.powerbot.script.Script;
import org.powerbot.script.rt4.*;

import java.sql.SQLOutput;
import java.util.Random;

@Script.Manifest(name="gemCutter", description ="Cuts gems")
public class gemCutter extends PollingScript<ClientContext> {
    Random rand = new Random();
    final int[] grandExchangeBankerIDs = {1613, 1633, 3089, 1634};
    final int chiselID = 1755;
    final int unRubyID = 1603;
    final int unDiamondID = -1;
    final int nRubyID = 1604;
    final int nDiamondID= -1;
    final int rubyTips = 9191;
    final int cuttingAnimation = 887;
    final int idleAnimation = -1;

    boolean mouseHop = false;
    int unRubyCount = -1;
    int nRubyCount = -1;
    long idleTime = System.currentTimeMillis();
    long cuttingTime = System.currentTimeMillis();
    long elapsedTime = -1;

    int triesUnnote = 0;
    int triesCut = 0;
    int antiBanID = 0;


    //TODO: Skal vi prøve å gjøre det på en bedre måte ifht hva scriptet skal gjøre nå? Task 1, 2, 3 osv?
    //TODO: lazyMode/focusedMode
    //TODO: Hoppe inn fra skjermen oppe, nede og ene siden med rng (MEST HØYRE)
    //TODO: Randomly hopp til et sted i nærheten av der den skal klikke (som om man klarer å dra litt forbi, før den går videre)
    // ctx.game.tab() returns currently open tab
    @Override
    public void start() {
        System.out.println("Bot is launching...");
    }

    @Override
    public void poll() {
        getRubyCount();
        if(ctx.players.local().animation() == cuttingAnimation){
            cuttingTime = System.currentTimeMillis();
        }
        if(ctx.players.local().animation() == idleAnimation){
            idleTime = System.currentTimeMillis();
        }
        elapsedTime = idleTime - cuttingTime;
        Condition.sleep(25);

        if(unRubyCount == 0 && nRubyCount == 0 || triesCut >= 5 || triesUnnote >= 5){
            while(true){
                ctx.input.move(765,randomNum(1,500));
                System.out.println("We're all out of rubies... Or did the script die? TriesCount: " + triesCut + ", " + triesUnnote);
                Condition.sleep(50000000);
            }
        }

        if(unRubyCount == 0) {
            triesUnnote++;
            triesCut = 0;
            System.out.println("Vi må nå unnote rubies");
            Condition.sleep(randomNum(250,12500));
            if(mouseHop == true){
                System.out.println("Return-hop");
                ctx.input.hop(764,randomNum(1,500));
                mouseHop = false;
            }
            Item _nRubyID = ctx.inventory.select().id(nRubyID).poll();
            _nRubyID.interact("Use");
            Condition.sleep(randomNum(220,1750));
            Npc banker = ctx.npcs.select().id(grandExchangeBankerIDs).nearest().poll();
            if(banker.inViewport()){
                System.out.println("Skal nå bruke ruby på banker");
                banker.interact("Use");
                Condition.sleep(randomNum(350,1250));
                if(randomNum(1,15) == 1){
                    Condition.sleep(randomNum(45,150));
                    ctx.chat.continueChat(false, "Yes");
                    System.out.println("Bruker mus til å trykke \"Yes\"..");
                }
                else{
                    Condition.sleep(randomNum(45,150));
                    ctx.chat.continueChat(true, "Yes"); //This does not work
                    System.out.println("Bruker tastatur til å trykke \"Yes\"..");
                }
                Condition.sleep(randomNum(2200,5500));
                getRubyCount();
            }
            else{
                while(true){
                    ctx.input.move(765, randomNum(1,500));
                    System.out.println("Finner ikke banker...");
                    Condition.sleep(50000000);
                }
            }
        }

        if(unRubyCount > 0 && elapsedTime >= randomNum(2500, 13000)) {
            triesCut++;
            triesUnnote = 0;
            Item _chisel = ctx.inventory.select().id(chiselID).poll();
            Item _unRubyID = ctx.inventory.select().id(unRubyID).poll();
            if(mouseHop == true){
                ctx.input.hop(764, randomNum(1,500));
                mouseHop = false;
            }
            //TODO: Sjekk at vi er i inventory
            _chisel.interact("Use");
            Condition.sleep(randomNum(150,1500));

            _unRubyID.interact("Use"); //TODO: Bruk den på en av rubiene nærmest chisel
            elapsedTime = 0;
            cuttingTime = System.currentTimeMillis();
            Condition.sleep(randomNum(450, 1450));
            System.out.println("skal nå trykke continue");
            Component makeAll = ctx.widgets.component(270,14);
            makeAll.click();
            Condition.sleep(randomNum(450,550));
            antiBan();
            //TODO: Her må vi sjekke hvor lenge det er siden personen var cutting sist
            //TODO: Må også passe på at den ikke kommer inn i metoden igjen
        }
        }

        public void preActionsCheck(){
            //TODO: Sjekk at chisel/noted er i riktig spot
            //TODO: Dra noted/chisel til riktig spot: https://www.powerbot.org/rsbot/releases/docs/org/powerbot/script/rt4/Inventory.html#drag-org.powerbot.script.rt4.Item-int-
            //TODO: Sjekk at vi er i inventory?
        }

        public void antiBan(){
            antiBanID = randomNum(1,randomNum(8,15));
            System.out.println("AntibanID: "+antiBanID);
            if(antiBanID == 1){
                System.out.println("Doing nothing...");
            }
            else if(antiBanID == 2){
                int sleepTime = randomNum(90000,199000);
                System.out.println("Sleeping for: " + sleepTime + "ms.");
                Condition.sleep(sleepTime);
            }else if(antiBanID == 3){
                //TODO: Move cursor to random spot on screen
            }
            else{
                System.out.println("Out of screen antiban");
                ctx.input.move(765,randomNum(1,500));
                mouseHop = true;
                Condition.sleep(randomNum(150,1500));
                ctx.input.defocus();
            }
        }

        public int getRubyCount(){
            unRubyCount = ctx.inventory.select().id(unRubyID).count();
            nRubyCount = ctx.inventory.select().id(nRubyID).count();
            return unRubyCount;
        }

        public void focusCheck(){
            if(ctx.input.isFocused()==false){
                ctx.input.focus();
            }
        }

        public int randomNum(int lower, int higher){
                int randInt = rand.nextInt(higher-lower) + lower;
                return randInt;
            }
}

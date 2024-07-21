import battleLogic.Battle;
import enemies.AbstractEnemy;
import enemies.PhysWeakEnemy;
import report.Report;
import teams.EnemyTeam;
import teams.PlayerTeam;

import java.util.ArrayList;

import static teams.EnemyTeam.*;
import static teams.PlayerTeam.*;

public class BattleSim {

    public static void main(String[] args) {
        //debugTeam();
        generateReportYunli();
    }
    
    public static void debugTeam() {
        Battle battle = new Battle();
        Battle.battle = battle;

        //battle.setPlayerTeam(TingyunYunliRobinHuohuo());
        //battle.setPlayerTeam(TopazYunliRobinHuohuo());
        //battle.setPlayerTeam(MarchYunliRobinHuohuo());
        //battle.setPlayerTeam(SparkleYunliRobinHuohuo());
        //battle.setPlayerTeam(SparkleYunliTingyunHuohuo());
        battle.setPlayerTeam(new PelaYunliTingyunHuohuoTeam().getTeam());

        ArrayList<AbstractEnemy> enemyTeam = new ArrayList<>();
        enemyTeam.add(new PhysWeakEnemy(0, 2));
        enemyTeam.add(new PhysWeakEnemy(1, 2));
        enemyTeam.add(new PhysWeakEnemy(2, 2));
        battle.setEnemyTeam(enemyTeam);

        battle.Start(5050);

        //Public calcs doc for Yunli: https://docs.google.com/document/d/1cCSiRX7-lfWp-fzhsaiUyhJuax0J9Dp2oU2t0CuVBDg/edit
    }

    public static void generateReportYunli() {
        PlayerTeam baselineTeam = new PelaYunliTingyunHuohuoTeam();
        ArrayList<PlayerTeam> otherTeams = new ArrayList<>();
        otherTeams.add(new TingyunYunliRobinHuohuoTeam());
        otherTeams.add(new TopazYunliRobinHuohuoTeam());
        otherTeams.add(new MarchYunliRobinHuohuoTeam());
        otherTeams.add(new SparkleYunliRobinHuohuoTeam());
        otherTeams.add(new SparkleYunliTingyunHuohuoTeam());

        ArrayList<EnemyTeam> enemyTeams = new ArrayList<>();
        enemyTeams.add(new PhysWeakTargets3());
        enemyTeams.add(new PhysWeakTargets2());
        enemyTeams.add(new PhysWeakTargets1());

        Report report = new Report(baselineTeam, otherTeams, enemyTeams, 5050);
        report.generateCSV();
    }


}
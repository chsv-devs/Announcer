package hancho.plugin.nukkit.announcer;

import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.scheduler.AsyncTask;
import cn.nukkit.scheduler.Task;
import cn.nukkit.utils.Config;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.ThreadLocalRandom;

public class Announcer extends PluginBase {
    public static final String PREFIX = "§l§f[ §g! §f] ";
    public ArrayList<String> announces;
    public HashSet<String> disabledPlayer;

    @Override
    public void onEnable() {
        Config config = this.getConfig();
        this.announces = config.get("announces" , new ArrayList<>());
        this.disabledPlayer = config.get("disabledPlayer", new HashSet<>());
        this.schedule();
    }

    @Override
    public void onDisable() {
        this.save(false);
    }

    public void save(boolean async) {
        if (async) {
            this.getServer().getScheduler().scheduleAsyncTask(this, new AsyncTask() {
                @Override
                public void onRun() {
                    save(false);
                }
            });
            return;
        }
        Config config = this.getConfig();
        config.set("announces", this.announces);
        config.set("disabledPlayer", this.disabledPlayer);
        config.save();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(cmd.getName().equals("공지")) {
            if(args.length < 1){
                this.sendHelpMessage(sender);
                return true;
            }
            String goal = args[0];
            if(sender.isOp()) {
                if (goal.equals("추가")) {
                    if (args.length < 2) {
                        this.sendHelpMessage(sender);
                        return true;
                    }
                    StringBuilder sb = new StringBuilder();
                    for (int i = 1; i < args.length; i++) {
                        sb.append(args[i]);
                        sb.append(" ");
                    }
                    this.addAnnounce(sb.toString());
                    sender.sendMessage(PREFIX + "성공적으로 추가되었습니다.");
                    return true;
                }
                if (goal.equals("삭제")) {
                    if (args.length < 2) {
                        this.sendHelpMessage(sender);
                        return true;
                    }
                    if (!isNumeric(args[1])) {
                        sender.sendMessage(PREFIX + "인덱스 값은 정수여야합니다.");
                        return true;
                    }
                    int index = Integer.parseInt(args[1]);
                    if (this.announces.size() <= index) {
                        sender.sendMessage(PREFIX + index + "는 유효하지 않는 인덱스입니다.");
                        return true;
                    }
                    this.removeAnnounce(index);
                    sender.sendMessage(PREFIX + "성공적으로 공지를 삭제하였습니다.");
                    return true;
                }
                if (goal.equals("목록")) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(PREFIX);
                    sb.append("공지 총 §3").append(announces.size()).append("§f개");
                    for (String announce : this.announces) {
                        sb.append("§r\n- ");
                        sb.append(announce);
                    }
                    sender.sendMessage(sb.toString());
                    return true;
                }
            }
            if(goal.equals("토글")){
                if(this.disabledPlayer.contains(sender.getName())){
                    this.disabledPlayer.remove(sender.getName());
                    sender.sendMessage(PREFIX + "공지가 켜졌습니다.");
                }else{
                    this.disabledPlayer.add(sender.getName());
                    sender.sendMessage(PREFIX + "공지가 꺼졌습니다.");
                }
                return true;
            }
        }
        return true;
    }

    public static boolean isNumeric(String num){
        try {
            Integer.parseInt(num);
            return true;
        } catch(NumberFormatException e){
            return false;
        }
    }

    public void sendHelpMessage(CommandSender sender){
        StringBuilder sb = new StringBuilder();
        if(sender.isOp()){
            sb.append(PREFIX + "/공지 추가 <내용>\n");
            sb.append(PREFIX + "/공지 삭제 <인덱스>\n");
            sb.append(PREFIX + "/공지 목록\n");
        }
        sb.append(PREFIX + "/공지 토글 - 공지를 끄고 킵니다");
        sender.sendMessage(sb.toString());
    }

    public void addAnnounce(String announce){
        this.announces.add(announce);
    }

    public void removeAnnounce(int index){
        this.announces.remove(index);
    }

    public void schedule(){
        this.getServer().getScheduler().scheduleDelayedRepeatingTask(this, new Task() {
            @Override
            public void onRun(int currentTick) {
                if(announces.size() == 0) return;
                String announce = PREFIX + announces.get(ThreadLocalRandom.current().nextInt(announces.size()));
                getServer().getOnlinePlayers().forEach((uuid, pl) -> {
                    if(disabledPlayer.contains(pl.getName())) return;
                    pl.sendMessage(announce);
                });
            }
        }, 20 * 60 * 2, 20 * 60 * 2);
    }
}

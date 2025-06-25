package blobproperties.entities.publicproperty;

import net.md_5.bungee.api.chat.ClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import us.mytheria.bloblib.api.BlobLibMessageAPI;
import us.mytheria.bloblib.entities.BlobExecutor;
import us.mytheria.bloblib.entities.ExecutorData;
import us.mytheria.bloblib.utilities.TextColor;
import us.mytheria.blobproperties.BlobPropertiesAPI;
import us.mytheria.blobproperties.director.PropertiesManagerDirector;
import us.mytheria.blobproperties.entities.BPProprietor;
import us.mytheria.blobproperties.entities.Property;
import us.mytheria.blobproperties.entities.Proprietor;

import java.util.ArrayList;
import java.util.List;

public class PublicPropertyCommand {
    private static PublicPropertyCommand instance;
    private static final BlobLibMessageAPI messageAPI = BlobLibMessageAPI.getInstance();
    private final PropertiesManagerDirector director;

    private PublicPropertyCommand(PropertiesManagerDirector director) {
        this.director = director;
    }

    public static PublicPropertyCommand getInstance(PropertiesManagerDirector director) {
        if (instance == null) {
            if (director == null)
                throw new NullPointerException("injected dependency is null");
            PublicPropertyCommand.instance = new PublicPropertyCommand(director);
        }
        return instance;
    }

    public static PublicPropertyCommand getInstance() {
        return getInstance(null);
    }


    public boolean command(ExecutorData data) {
        BlobExecutor executor = data.executor();
        CommandSender sender = data.sender();
        String[] args = data.args();
        if (args.length == 0) {
            debug(sender);
            return true;
        }
        if (!executor.isInstanceOfPlayer(sender)) {
            return false;
        }
        Player player = (Player) sender;
        String arg = args[0];
        if (args.length == 1) {
            if (arg.equalsIgnoreCase("home")) {
                director.getListenerManager().getPublicPropertyHome().open(player);
                return true;
            }
            return false;
        }
        String subArg = args[1];
        if (args.length == 2) {
            if (!arg.equalsIgnoreCase("party"))
                return false;
            switch (subArg.toLowerCase()) {
                case "leave" -> {
                    BPProprietor senderProprietor = director.getProprietorManager().getProprietor(player);
                    if (senderProprietor == null) {
                        messageAPI.getMessage("BlobProprietor.Not-Inside-Cache", player).handle(player);
                        return true;
                    }
                    PublicParty party = senderProprietor.getCurrentlyAttending();
                    if (party == null) {
                        messageAPI.getMessage("BlobProprietor.Not-Attending-Party", player).handle(player);
                        return true;
                    }
                    if (party.getOwnerName().equals(player.getName())) {
                        messageAPI.getMessage("BlobProprietor.Party-Leader-Cannot-Leave", player).handle(player);
                        return true;
                    }
                    party.depart(senderProprietor, false);
                    return true;
                }
                case "disband" -> {
                    BPProprietor host = director.getProprietorManager().getProprietor(player);
                    if (host == null) {
                        messageAPI.getMessage("BlobProprietor.Not-Inside-Cache", player).handle(player);
                        return true;
                    }
                    PublicParty party = host.getCurrentlyAttending();
                    if (party == null) {
                        messageAPI.getMessage("BlobProprietor.Not-Attending-Party", player).handle(player);
                        return true;
                    }
                    if (!party.getOwnerName().equals(player.getName())) {
                        messageAPI.getMessage("BlobProprietor.Not-Party-Leader", player).handle(player);
                        return true;
                    }
                    party.disband();
                }
                default -> {
                    debug(player);
                    return true;
                }
            }
        }
        if (args.length == 3) {
            if (arg.equalsIgnoreCase("party")) {
                String thirdArg = args[2];
                switch (subArg.toLowerCase()) {
                    case "invite" -> {
                        Player target = Bukkit.getPlayer(thirdArg);
                        if (target == null) {
                            messageAPI.getMessage("Player.Not-Found", player).handle(player);
                            return true;
                        }
                        BPProprietor host = director.getProprietorManager().getProprietor(player);
                        if (host == null) {
                            messageAPI.getMessage("BlobProprietor.Not-Inside-Cache", player).handle(player);
                            return true;
                        }
                        BPProprietor guest = director.getProprietorManager().getProprietor(target);
                        if (guest == null) {
                            messageAPI.getMessage("BlobProprietor.Not-Inside-Cache-Others", player)
                                    .modify(s -> s.replace("%player%", target.getName()))
                                    .handle(player);
                            return true;
                        }
                        PublicParty party = host.getCurrentlyAttending();
                        Property currentlyAt = host.getCurrentlyAt();
                        if (currentlyAt == null) {
                            messageAPI.getMessage("BlobProprietor.Not-Inside-Property", player).handle(player);
                            return true;
                        }
                        if (!host.ownsPublicProperty(currentlyAt)) {
                            messageAPI.getMessage("BlobProprietor.Not-Inside-Own-Property", player).handle(player);
                            return true;
                        }
                        if (party == null)
                            party = new PublicParty(host, currentlyAt);
                        messageAPI.getMessage("BlobProprietor.Invited", player)
                                .modify(s -> s.replace("%player%", target.getName()))
                                .handle(player);
                        messageAPI.getMessage("BlobProprietor.Received-Invite", target)
                                .modify(s -> s.replace("%player%", player.getName()))
                                .onClick(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/publicproperty party visit " + player.getName()))
                                .handle(target);
                        guest.addPendingInvite(host, party);
                        return true;
                    }
                    case "visit" -> {
                        Player target = Bukkit.getPlayer(thirdArg);
                        if (target == null) {
                            messageAPI.getMessage("Player.Not-Found", player).handle(player);
                            return true;
                        }
                        BPProprietor guest = director.getProprietorManager().getProprietor(player);
                        if (guest == null) {
                            messageAPI.getMessage("BlobProprietor.Not-Inside-Cache", player).handle(player);
                            return true;
                        }
                        if (!guest.hasPendingInvite(target)) {
                            messageAPI.getMessage("BlobProprietor.No-Pending-Invite", player).handle(player);
                            return true;
                        }
                        BPProprietor host = director.getProprietorManager().getProprietor(target);
                        if (host == null) {
                            messageAPI.getMessage("BlobProprietor.Not-Inside-Cache-Others",
                                            player)
                                    .modify(s -> s.replace("%player%", target.getName()))
                                    .handle(player);
                            return true;
                        }
                        PublicParty party = host.getCurrentlyAttending();
                        if (party == null) {
                            messageAPI.getMessage("BlobProprietor.Other-Not-Attending-Party", player)
                                    .modify(s -> s.replace("%player%", target.getName()))
                                    .handle(player);
                            return true;
                        }
                        PublicParty oldParty = guest.getCurrentlyAttending();
                        if (oldParty != null) {
                            oldParty.stepOut(guest, false);
                            oldParty.unallow(guest);
                        }
                        if (!party.lodge(guest) && guest.getCurrentlyAt() != null) {
                            guest.stepOut(null);
                        }
                        guest.removePendingInvite(host);
                        return true;
                    }
                    case "kick" -> {
                        Player target = Bukkit.getPlayer(thirdArg);
                        if (target == null) {
                            messageAPI.getMessage("Player.Not-Found", player).handle(player);
                            return true;
                        }
                        BPProprietor host = director.getProprietorManager().getProprietor(player);
                        if (host == null) {
                            messageAPI.getMessage("BlobProprietor.Not-Inside-Cache", player).handle(player);
                            return true;
                        }
                        BPProprietor guest = director.getProprietorManager().getProprietor(target);
                        if (guest == null) {
                            messageAPI.getMessage("BlobProprietor.Not-Inside-Cache-Others")
                                    .modify(s -> s.replace("%player%", target.getName()))
                                    .handle(player);
                            return true;
                        }
                        PublicParty party = host.getCurrentlyAttending();
                        if (party == null) {
                            messageAPI.getMessage("BlobProprietor.Not-Attending-Party", player).handle(player);
                            return true;
                        }
                        if (guest.getCurrentlyAttending() != null && guest.getCurrentlyAttending().equals(party)
                                && guest.getCurrentlyAt() != null &&
                                guest.getCurrentlyAt().identifier().equals(
                                        party.getProperty().getKey())) {
                            party.stepOut(guest, true);
                        }
                        guest.removePendingInvite(host);
                        party.unallow(guest);
                        return true;
                    }
                    default -> {
                        debug(player);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void debug(CommandSender sender) {
        sender.sendMessage(translate("&e/publicproperty home)"));
        sender.sendMessage(translate("&e/publicproperty party invite <player>"));
        sender.sendMessage(translate("&e/publicproperty party visit <player>"));
        sender.sendMessage(translate("&e/publicproperty party kick <player>"));
        sender.sendMessage(translate("&e/publicproperty party disband"));
    }

    private String translate(String message) {
        return TextColor.PARSE(message);
    }

    public List<String> tabCompleter(ExecutorData data) {
        String[] args = data.args();
        BlobExecutor executor = data.executor();
        CommandSender sender = data.sender();
        List<String> suggestions = new ArrayList<>();
        switch (args.length) {
            case 1 -> {
                suggestions.add("home");
                suggestions.add("party");
            }
            case 2 -> {
                String arg = args[0];
                if (arg.equalsIgnoreCase("party")) {
                    suggestions.add("invite");
                    suggestions.add("visit");
                    suggestions.add("kick");
                    suggestions.add("disband");
                    suggestions.add("leave");
                }
                return suggestions;
            }
            case 3 -> {
                String arg = args[0];
                String subArg = args[1];
                if (arg.equalsIgnoreCase("party")) {
                    switch (subArg) {
                        case "invite" -> {
                            if (!executor.isInstanceOfPlayer(sender))
                                return suggestions;
                            Player senderPlayer = (Player) sender;
                            Proprietor owner = BlobPropertiesAPI.getInstance().getProprietor(((Player) sender));
                            if (owner == null)
                                return suggestions;
                            Bukkit.getOnlinePlayers().stream()
                                    .filter(player -> !player.getName().equals(senderPlayer.getName()))
                                    .forEach(player -> suggestions.add(player.getName()));
                            return suggestions;
                        }
                        case "visit" -> {
                            if (!executor.isInstanceOfPlayer(sender))
                                return suggestions;
                            Player senderPlayer = (Player) sender;
                            Proprietor owner = BlobPropertiesAPI.getInstance().getProprietor((senderPlayer));
                            if (owner == null)
                                return suggestions;
                            suggestions.addAll(owner.getPendingInvites());
                            return suggestions;
                        }
                        case "kick" -> {
                            if (!executor.isInstanceOfPlayer(sender))
                                return suggestions;
                            Player senderPlayer = (Player) sender;
                            Proprietor proprietor = director
                                    .getProprietorManager().getProprietor(senderPlayer);
                            PublicParty party = proprietor.getCurrentlyAttending();
                            if (party != null)
                                party.forEachAllowed(allowed -> suggestions.add(allowed.getName()));
                        }
                    }
                }
            }
        }
        return suggestions;
    }
}

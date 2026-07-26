package org.omegachess.cli;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

import org.omegachess.core.GameEncoding;
import org.omegachess.core.GameLogic;
import org.omegachess.core.GameState;
import org.omegachess.core.Move;

/* Persistent JSONL command-line interface for Omega Chess.

   Protocol rules:
     - Read one JSON object per line from standard input.
     - Write exactly one JSON object per request to standard output.
     - Flush standard output after every response.
     - Write diagnostics only to standard error.

   Requests are deliberately flat JSON objects whose values are strings. */
public final class GameLogicCli
  {
    private static final String VERSION = "1.0";
    private static final GameState STATE = new GameState();
    private static final Move[] MOVE_BUFFER = new Move[GameState._MAX_MOVES];
    private static final Move[] LEGALITY_BUFFER = new Move[GameState._MAX_MOVES];

    private GameLogicCli()
      {
      }

    public static void main(String[] args) throws IOException
      {
        try( BufferedReader input = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
             BufferedWriter output = new BufferedWriter(new OutputStreamWriter(System.out, StandardCharsets.UTF_8)) )
          {
            String line;

            while((line = input.readLine()) != null)
              {
                line = line.trim();

                if(line.isEmpty())
                  continue;

                boolean shouldQuit = false;
                String response;

                try
                  {
                    Map<String, String> request = FlatStringJson.parseObject(line);
                    String command = require(request, "cmd");
                    response = dispatch(command, request);
                    shouldQuit = "quit".equals(command);
                  }
                catch(RuntimeException ex)
                  {
                    response = errorResponse(ex);
                  }

                output.write(response);
                output.newLine();
                output.flush();

                if(shouldQuit)
                  break;
              }
          }
      }

    private static String dispatch(String command, Map<String, String> request)
      {
        switch(command)
          {
            case "ping":
              return "{\"ok\":true,\"cmd\":\"ping\"," + "\"version\":" + jsonString(VERSION) + "}";

            case "startpos":
              return startposResponse();

            case "side_to_move":
              return sideToMoveResponse(request);

            case "legal_moves":
              return legalMovesResponse(request);

            case "apply_move":
              return applyMoveResponse(request);

            case "terminal":
              return terminalResponse(request);

            case "draw":
              return drawResponse(request);

            case "print_move":
              return printMoveResponse(request);

            case "quit":
              return "{\"ok\":true,\"cmd\":\"quit\"}";

            default:
              throw new IllegalArgumentException("Unknown command: " + command);
          }
      }

    private static String startposResponse()
      {
        GameState start = GameLogic.createStartingPosition();
        String stateHex = bytesToHex(GameEncoding.encodeState(start));
        return "{\"ok\":true,\"cmd\":\"startpos\"," + "\"state\":" + jsonString(stateHex) + "}";
      }

    private static String sideToMoveResponse(Map<String, String> request)
      {
        decodeStateIntoWorkingState(require(request, "state"));
        String side = STATE.isWhiteToMove() ? "white" : "black";
        return "{\"ok\":true,\"cmd\":\"side_to_move\"," + "\"side\":" + jsonString(side) + "}";
      }

    private static String legalMovesResponse(Map<String, String> request)
      {
        decodeStateIntoWorkingState(require(request, "state"));

        int count = GameLogic.legalMoves(STATE, MOVE_BUFFER);
        StringBuilder response = new StringBuilder(64 + count * 10);

        response.append("{\"ok\":true,\"cmd\":\"legal_moves\",");
        response.append("\"count\":").append(count).append(',');
        response.append("\"moves\":[");

        for(int i = 0; i < count; i++)
          {
            if(i > 0)
              response.append(',');

            response.append('"');
            appendMoveHex(response, MOVE_BUFFER[i]);
            response.append('"');
          }

        response.append("]}");
        return response.toString();
      }

    private static String applyMoveResponse(Map<String, String> request)
      {
        String stateHex = require(request, "state");
        decodeStateIntoWorkingState(stateHex);

        Move requestedMove = decodeMoveHex(require(request, "move"));

        boolean legal = GameLogic.applyLegalMove(STATE, requestedMove, LEGALITY_BUFFER);

        String resultState = legal ? bytesToHex(GameEncoding.encodeState(STATE)) : normalizeHex(stateHex);

        return "{\"ok\":true,\"cmd\":\"apply_move\"," + "\"legal\":" + legal + ',' + "\"state\":" + jsonString(resultState) + "}";
      }

    private static String terminalResponse(Map<String, String> request)
      {
        decodeStateIntoWorkingState(require(request, "state"));

        int result = GameLogic.terminalStatus(STATE);
        boolean terminal = result != GameState.GAME_ONGOING;

        return "{\"ok\":true,\"cmd\":\"terminal\"," +
               "\"terminal\":" + terminal + ',' +
               "\"result\":" + result + ',' + "\"result_name\":" + jsonString(resultName(result)) + "}";
      }

    private static String drawResponse(Map<String, String> request)
      {
        decodeStateIntoWorkingState(require(request, "state"));

        StringBuilder response = new StringBuilder(256);
        response.append("{\"ok\":true,\"cmd\":\"draw\",");
        response.append("\"side\":");
        response.append(jsonString(STATE.isWhiteToMove() ? "white" : "black"));
        response.append(",\"rows\":[");

        for(int row = 11; row >= 0; row--)
          {
            if(row < 11)
              response.append(',');

            StringBuilder boardRow = new StringBuilder(12);

            for(int col = 0; col < 12; col++)
              {
                int index = row * 12 + col;
                boardRow.append(pieceCharacter(index));
              }

            response.append(jsonString(boardRow.toString()));
          }

        response.append("]}");
        return response.toString();
      }

    private static String printMoveResponse(Map<String, String> request)
      {
        Move move = decodeMoveHex(require(request, "move"));
        String notation = moveNotation(move);

        return "{\"ok\":true,\"cmd\":\"print_move\"," +
               "\"from\":" + move.from + ',' +
               "\"to\":" + move.to + ',' +
               "\"promo\":" + (move.promo & 0xFF) + ',' +
               "\"notation\":" + jsonString(notation) + "}";
      }

    private static void decodeStateIntoWorkingState(String stateHex)
      {
        byte[] encoded = hexToBytesExact(stateHex, GameState._GAMESTATE_BYTE_SIZE, "state");

        GameEncoding.decodeState(encoded, STATE);
      }

    private static Move decodeMoveHex(String moveHex)
      {
        byte[] encoded = hexToBytesExact(moveHex, GameState._MOVE_BYTE_SIZE, "move");
        return GameEncoding.decodeMove(encoded);
      }

    private static String resultName(int result)
      {
        switch(result)
          {
            case GameState.GAME_ONGOING:
              return "ongoing";
            case GameState.GAME_OVER_WHITE_WINS:
              return "white_wins";
            case GameState.GAME_OVER_BLACK_WINS:
              return "black_wins";
            case GameState.GAME_OVER_STALEMATE:
              return "draw";
            default:
              return "unknown";
          }
      }

    private static char pieceCharacter(int index)
      {
        if(STATE.oob(index))
          return ' ';

        if(STATE.isEmpty(index))
          return '.';

        char piece;

        if(STATE.isPawn(index))
          piece = 'P';
        else if(STATE.isKnight(index))
          piece = 'N';
        else if(STATE.isChampion(index))
          piece = 'C';
        else if (STATE.isWizard(index))
          piece = 'W';
        else if (STATE.isBishop(index))
          piece = 'B';
        else if (STATE.isRook(index))
          piece = 'R';
        else if (STATE.isQueen(index))
          piece = 'Q';
        else if (STATE.isKing(index))
          piece = 'K';
        else
          piece = '?';

        return STATE.isWhite(index) ? piece : Character.toLowerCase(piece);
      }

    private static String moveNotation(Move move)
      {
        StringBuilder notation = new StringBuilder(12);
        notation.append(squareName(move.from));
        notation.append('-');
        notation.append(squareName(move.to));

        if(move.promo != GameState._NO_PROMO)
          {
            notation.append('=');
            notation.append(promotionName(move.promo));
          }

        return notation.toString();
      }

    private static String squareName(int index)
      {
        int row = index / 12;
        int col = index % 12;

        if(row >= 1 && row <= 10 && col >= 1 && col <= 10)
          {
            char file = (char) ('A' + col - 1);
            return Character.toString(file) + row;
          }
        else if(index == 0)
          return "W1";
        else if(index == 11)
          return "W2";
        else if(index == 132)                                       //  Yes, Omega Chess corner notation runs "counter clockwise."
          return "W4";
        else if(index == 143)
          return "W3";

        return "@" + index;                                         //  Should never happen.
      }

    private static char promotionName(byte promotion)
      {
        switch(promotion)
          {
            case GameState._PROMO_KNIGHT:
              return 'N';
            case GameState._PROMO_CHAMPION:
              return 'C';
            case GameState._PROMO_WIZARD:
              return 'W';
            case GameState._PROMO_BISHOP:
              return 'B';
            case GameState._PROMO_ROOK:
              return 'R';
            case GameState._PROMO_QUEEN:
              return 'Q';
            default:
              return '?';
          }
      }

    private static String require(Map<String, String> request, String key)
      {
        String value = request.get(key);

        if(value == null)
          throw new IllegalArgumentException("Missing required string field: " + key);

        return value;
      }

    private static String normalizeHex(String value)
      {
        return value.trim().toLowerCase();
      }

    private static byte[] hexToBytesExact(String value, int expectedBytes, String fieldName)
      {
        String hex = normalizeHex(value);
        int expectedCharacters = expectedBytes * 2;

        if(hex.length() != expectedCharacters)
          throw new IllegalArgumentException(fieldName + " must contain exactly " + expectedCharacters + " hexadecimal characters");

        byte[] result = new byte[expectedBytes];

        for(int i = 0; i < expectedBytes; i++)
          {
            int high = Character.digit(hex.charAt(i * 2), 16);
            int low = Character.digit(hex.charAt(i * 2 + 1), 16);

            if(high < 0 || low < 0)
              throw new IllegalArgumentException(fieldName + " contains a non-hexadecimal character");

            result[i] = (byte) ((high << 4) | low);
          }

        return result;
      }

    private static String bytesToHex(byte[] bytes)
      {
        StringBuilder result = new StringBuilder(bytes.length * 2);

        for(byte value : bytes)
          appendHexByte(result, value & 0xFF);

        return result.toString();
      }

    private static void appendMoveHex(StringBuilder output, Move move)
      {
        if(move == null)
          throw new IllegalStateException("Game logic returned a null move");

        appendHexByte(output, move.from);
        appendHexByte(output, move.to);
        appendHexByte(output, move.promo & 0xFF);
        return;
      }

    private static void appendHexByte(StringBuilder output, int value)
      {
        if(value < 0 || value > 0xFF)
          throw new IllegalArgumentException("Value does not fit in one byte: " + value);

        final char[] digits = "0123456789abcdef".toCharArray();
        output.append(digits[(value >>> 4) & 0x0F]);
        output.append(digits[value & 0x0F]);
        return;
      }

    private static String errorResponse(RuntimeException ex)
      {
        String type = ex.getClass().getSimpleName();
        String message = ex.getMessage();

        if(message == null || message.isEmpty())
          message = type;

        return "{\"ok\":false,\"error_type\":" + jsonString(type) + ",\"error\":" + jsonString(message) + "}";
      }

    private static String jsonString(String value)
      {
        StringBuilder output = new StringBuilder(value.length() + 2);
        output.append('"');

        for(int i = 0; i < value.length(); i++)
          {
            char ch = value.charAt(i);

            switch(ch)
              {
                case '"':
                  output.append("\\\"");
                  break;
                case '\\':
                  output.append("\\\\");
                  break;
                case '\b':
                  output.append("\\b");
                  break;
                case '\f':
                  output.append("\\f");
                  break;
                case '\n':
                  output.append("\\n");
                  break;
                case '\r':
                  output.append("\\r");
                  break;
                case '\t':
                  output.append("\\t");
                  break;
                default:
                  if(ch < 0x20)
                    output.append(String.format("\\u%04x", (int) ch));
                  else
                    output.append(ch);
                  break;
              }
          }

        output.append('"');
        return output.toString();
      }

    /**
     * Minimal parser for a flat JSON object whose values are strings.
     * This is sufficient for the CLI protocol and avoids adding a JSON
     * dependency to the executable JAR.
     */
    private static final class FlatStringJson
      {
        private final String source;
        private int position;

        private FlatStringJson(String source)
          {
            this.source = source;
          }

        static Map<String, String> parseObject(String source)
          {
            FlatStringJson parser = new FlatStringJson(source);
            Map<String, String> result = parser.readObject();
            parser.skipWhitespace();

            if(!parser.atEnd())
              throw parser.error("Unexpected trailing characters");

            return result;
          }

        private Map<String, String> readObject()
          {
            LinkedHashMap<String, String> result = new LinkedHashMap<String, String>();

            skipWhitespace();
            expect('{');
            skipWhitespace();

            if(peek('}'))
              {
                position++;
                return result;
              }

            while(true)
              {
                skipWhitespace();
                String key = readString();
                skipWhitespace();
                expect(':');
                skipWhitespace();
                String value = readString();

                if(result.put(key, value) != null)
                  throw error("Duplicate key: " + key);

                skipWhitespace();

                if(peek(','))
                  {
                    position++;
                    continue;
                  }

                expect('}');
                return result;
              }
          }

        private String readString()
          {
            expect('"');
            StringBuilder result = new StringBuilder();

            while(!atEnd())
              {
                char ch = source.charAt(position++);

                if(ch == '"')
                  return result.toString();

                if(ch != '\\')
                  {
                    if(ch < 0x20)
                      throw error("Control character in JSON string");

                    result.append(ch);
                    continue;
                  }

                if(atEnd())
                  throw error("Incomplete JSON escape");

                char escaped = source.charAt(position++);

                switch(escaped)
                  {
                    case '"':
                    case '\\':
                    case '/':
                      result.append(escaped);
                      break;
                    case 'b':
                      result.append('\b');
                      break;
                    case 'f':
                      result.append('\f');
                      break;
                    case 'n':
                      result.append('\n');
                      break;
                    case 'r':
                      result.append('\r');
                      break;
                    case 't':
                      result.append('\t');
                      break;
                    case 'u':
                      result.append(readUnicodeEscape());
                      break;
                    default:
                      throw error("Unsupported JSON escape: \\" + escaped);
                  }
              }

            throw error("Unterminated JSON string");
          }

        private char readUnicodeEscape()
          {
            if(position + 4 > source.length())
              throw error("Incomplete Unicode escape");

            int value = 0;

            for(int i = 0; i < 4; i++)
              {
                int digit = Character.digit(source.charAt(position++), 16);

                if(digit < 0)
                  throw error("Invalid Unicode escape");

                value = (value << 4) | digit;
              }

            return (char) value;
          }

        private void skipWhitespace()
          {
            while(!atEnd() && Character.isWhitespace(source.charAt(position)))
              position++;
            return;
          }

        private void expect(char expected)
          {
            if(atEnd() || source.charAt(position) != expected)
              throw error("Expected '" + expected + "'");

            position++;
            return;
          }

        private boolean peek(char expected)
          {
            return !atEnd() && source.charAt(position) == expected;
          }

        private boolean atEnd()
          {
            return position >= source.length();
          }

        private IllegalArgumentException error(String message)
          {
            return new IllegalArgumentException(message + " at character " + position);
          }
      }
  }

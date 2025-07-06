package engineer.mkitsoukou.tika.application.shared;

@FunctionalInterface
public interface UseCase<I, O> {
  O execute(I input);
}
